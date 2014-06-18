package org.dainst.gazetteer.controller;

import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.dainst.gazetteer.dao.HarvesterDefinitionRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.dainst.gazetteer.match.AutoMatchService;
import org.dainst.gazetteer.search.ElasticSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * Handles requests for the application home page.
 */
@Controller
public class AdminController {

	private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private HarvesterDefinitionRepository harvesterDefinitionDao;
	
	@Autowired
	private ElasticSearchService esService;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Autowired
	private Merger merger;
	
	@Autowired
	private AutoMatchService autoMatchService;
	
	@Value("${geonamesSolrUri}")
	private String geonamesSolrUri;
	
	@RequestMapping(value="/admin/reindex", method=RequestMethod.POST)
	@ResponseBody
	public String reindex() {
		
		esService.reindexAllPlaces();
		
		return "OK: reindexing started";
		
	}
	
	@RequestMapping(value="/admin/toggleHarvester/{name}", method=RequestMethod.POST)
	@ResponseBody
	public String toggleHarvester(@PathVariable String name) {
		
		HarvesterDefinition harvesterDefinition = harvesterDefinitionDao
				.getByName(name);
		harvesterDefinition.setEnabled(!harvesterDefinition.isEnabled());
		harvesterDefinitionDao.save(harvesterDefinition);
		
		return String.format("OK: set %s to enabled = %s",
				harvesterDefinition.getName(),
				harvesterDefinition.isEnabled());
		
	}
	
	@RequestMapping(value="/admin/resetHarvester/{name}", method=RequestMethod.POST)
	@ResponseBody
	public String resetHarvester(@PathVariable String name) {
		
		HarvesterDefinition harvesterDefinition = harvesterDefinitionDao
				.getByName(name);
		harvesterDefinition.setLastHarvestedDate(null);
		harvesterDefinition.setEnabled(true);
		harvesterDefinitionDao.save(harvesterDefinition);
		
		return String.format("OK: reset %s",
				harvesterDefinition.getName());
		
	}
	
	@RequestMapping(value="/admin/importGeonames", method=RequestMethod.POST)
	@ResponseBody
	public String importGeonames() {
		
		List<Place> places = placeDao.findByPrefLocationIsNullAndIdsContext("geonames");
		
		logger.debug("found {} places with geonames-id and no location", places.size());
		
		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		Client client = Client.create(config);
		WebResource api = client.resource(geonamesSolrUri).path("select").queryParam("wt", "json");
		api.addFilter(new ClientFilter() {
		    @Override
		    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {
		    	ClientResponse response = getNext().handle(request);
		    	response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
		    	return response;
		    }
		});
		
		int count = 0;
		for (Place place : places) {
			ObjectNode node = api.queryParam("q", String.format("seriennummer:%s", place.getIdentifier("geonames")))
					.get(ObjectNode.class);
			if (node.get("response").get("numFound").asInt() > 0) {
				logger.debug("found {} candidates.", node.get("response").get("numFound"));
				JsonNode entries = node.get("response").get("docs");
				for (JsonNode entry : entries) {
					if (entry.has("longitude")) {
						logger.debug("found coordinates in doc: {}", entry);
						place.setPrefLocation(new Location(entry.get("longitude").get(0).asDouble(),
								entry.get("latitude").get(0).asDouble()));
						placeDao.save(place);
						count ++;
						break;
					}
				}
			}
		}
		
		return String.format("OK: imported %s locations", count);
		
	}
	
	@RequestMapping(value="/admin/automatch", method=RequestMethod.POST)
	@ResponseBody
	public String automatch() {
		
		autoMatchService.runAutoMatch(placeDao, merger);
		
		return "auto matching started in separate thread.";
		
	}
	
	@RequestMapping(value="/admin/calculateChildren", method=RequestMethod.POST)
	@ResponseBody
	public String calculateChildren() {
		
		long time = System.currentTimeMillis();
		
		Iterable<Place> places = placeDao.findAll();
		for (Place place : places) {
			try {
				int size = placeDao.findByParent(place.getId()).size();
				place.setChildren(size);
				placeDao.save(place);
			} catch (NullPointerException e) {
				logger.warn("Could not find parent {} for {}", place.getParent(), place);
			}
		}
		
		String message = "OK: finished calculating children in " + (System.currentTimeMillis() - time) + "ms";
		logger.info(message);
		
		return message;
	}
	
	@RequestMapping(value="/admin/generateLinks", method=RequestMethod.POST)
	@ResponseBody
	public String generateLinks() {
		
		generateLinks("pleiades", "http://pleiades.stoa.org/places/", "owl:sameAs");
		generateLinks("geonames", "http://sws.geonames.org/", "owl:sameAs");
		
		return "OK: finished generating Links";
		
	}
	
	private void generateLinks(String context, String baseUri, String predicate) {
		List<Place> places = placeDao.findByIdsContext(context);
		outer: for (Place place : places) {
			for (Link link : place.getLinks()) {
				if (link.getObject().startsWith(baseUri))
					continue outer;
			}
			Link newLink = new Link();
			newLink.setObject(baseUri + place.getIdentifier(context));
			newLink.setPredicate(predicate);
			place.addLink(newLink);
			placeDao.save(place);
			logger.debug("Generated Link: {}", newLink);
		}
	}

}
