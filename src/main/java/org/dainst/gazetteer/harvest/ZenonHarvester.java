package org.dainst.gazetteer.harvest;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import javax.ws.rs.core.MediaType;

import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class ZenonHarvester implements Harvester {

	private static Logger logger = LoggerFactory.getLogger(ArachneHarvester.class);
	
	public final static String RESOURCE_URI = "http://testopac.dainst.org:8080/elwms-zenon/thesaurus/";
	
	public final static List<String> SKIP_TITLES = Arrays.asList(
		"ZZ--Regionen",
		"Regionen",
		"Orte",
		"Gebiete",
		"Orte [A-Z]+-[A-Z]+"
	);
	
	public final static List<String> ROOT_IDS = Arrays.asList(
		"xTopLand" // Klassische Archäologie -> Topographie -> Länder mit Gebieten und Orten
		//"3.00.01", // Iberische Halbinsel -> Topographie
		//"zTopog", // Topograhpie
		//"4.02" // Thesaurus Eurasien-Abteilung -> Regionen/Länder/Orte
	);
	
	private WebResource api;
	
	private IdGenerator idGenerator;
	
	private Queue<Place> queue = new ArrayDeque<Place>();
	
	public ZenonHarvester() {
		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		Client client = Client.create(config);
		api = client.resource(RESOURCE_URI);
	}
	
	@Override
	public void harvest(Date date) {
		
		for (String rootId : ROOT_IDS) {
			createPlacesRecursively(rootId, null);
		}
		
	}

	@Override
	public List<Place> getNextPlaces() {
	
		logger.debug("queue size in getNextPlaces(): {}", queue.size());
		
		if (queue.isEmpty()) {
			return null;
		} else {
			ArrayList<Place> result = new ArrayList<Place>();
			result.add(queue.poll());
			return result;
		}
		
	}

	@Override
	public void close() {
		// nothing to do
	}

	@Override
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	private void createPlacesRecursively(String id, Place parent) {
		
		logger.debug("creating place with id {}", id);

		Builder builder = api.path("resource/" + id + "/").accept(MediaType.APPLICATION_JSON_TYPE);
		
		ObjectNode placeNode = null;	
		try {
			placeNode = builder.get(ObjectNode.class);
		} catch (Exception e) {
			logger.warn("Unable to resolve resource " + id +". Skipping ...", e);
			return;
		}
		
		if (placeNode == null) {
			logger.warn("Unable to parse JSON for resource {}. Skipping ...", id);
			return;
		}
		
		Place place = new Place();
		
		place.setId(idGenerator.generate(place));
		place.addThesaurus("zenon");
		
		JsonNode marcNodes = placeNode.get("data").get("marc:datafield");
		for (JsonNode marcNode : marcNodes) {
			String tag = marcNode.get("@tag").asText();

			// inline children
			if ("552".equals(tag)) {
				Place child = new Place();
				String text = marcNode.get("marc:subfield").get("#text").asText();
				if (text != null && !text.isEmpty()) {
					PlaceName name = new PlaceName(text);
					child.setPrefName(name);
					child.setParent(place.getId());
					child.setId(idGenerator.generate(child));
					queue.add(child);
					logger.debug("added inline child to queue: {}", child);
				}
			
			// place names
			} else if ("551".equals(tag)) {
				PlaceName name = new PlaceName();
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : marcSubNodes) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("a".equals(code)) {
						name.setTitle(text);
					} else if ("9".equals(code)) {
						name.setLanguage(text);
					}
					if (name.getTitle() != null && !name.getTitle().isEmpty()) {
						if (place.getPrefName() == null) place.setPrefName(name);
						else place.addName(name);
					}
				}
				
			}
			
			// TODO further place data
			
		}
		
		Identifier zenonId = new Identifier();
		zenonId.setContext("zenon-thesaurus");
		zenonId.setValue(id);
		place.addIdentifier(zenonId);
		
		logger.debug("place: {}", place);
		
		if (parent != null) place.setParent(parent.getId());
		queue.add(place);
		
		// recurse into subtree
		logger.debug("getting {}", api.path("children/" + id + "/").queryParam("format", "standard").toString());
		builder = api.path("children/" + id + "/").queryParam("format", "standard")
				.accept(MediaType.APPLICATION_JSON_TYPE);
		JsonNode childNodes = null;
		try {
			childNodes = builder.get(ObjectNode.class).get("data");
		} catch(UniformInterfaceException e) {
			logger.warn("Unable to get children for resource " + id +". Skipping ...", e);
			return;
		} catch(ClientHandlerException e) {
			logger.warn("Unable to get children for resource " + id +". Skipping ...", e);
			return;
		} catch (NullPointerException e) {
			logger.warn("Unable to parse JSON for children for resource {}. Skipping ...", id);
			return;
		}
		
		if (childNodes == null) {
			logger.warn("Unable to parse JSON for children for resource {}. Skipping ...", id);
			return;
		}
		
		for (JsonNode childNode : childNodes) {
			
			String childId = childNode.get("id").asText();
			boolean childLeaf = false;
			JsonNode leafNode = childNode.get("leaf");
			if (leafNode != null) childLeaf = leafNode.asBoolean();
			if (!childLeaf) {
				createPlacesRecursively(childId, place);
			}
			
		}
		
	}

}
