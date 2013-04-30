package org.dainst.gazetteer.harvest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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

	private static Logger logger = LoggerFactory.getLogger(ZenonHarvester.class);
	
	public final static String RESOURCE_URI = "http://testopac.dainst.org:8080/elwms-zenon/thesaurus/";
	
	public final static List<String> SKIP_TITLES = Arrays.asList(
		"ZZ-.+",
		".*Regionen.*",
		"Orte",
		"Gebiete",
		"Orte [A-Z]+-[A-Z]+",
		"Topograhpie"
	);
	
	public final static Map<String,String> PLACE_TYPES;
	static {
		Map<String, String> placeTypes = new HashMap<String,String>();
		placeTypes.put("Kontinent", "continent");
		placeTypes.put("ZZ-.+", "region");
		placeTypes.put(".*Regionen.*", "region");
		placeTypes.put("Orte", "city");
		placeTypes.put("Gebiete", "region");
		placeTypes.put("Orte [A-Z]+-[A-Z]+", "city");
		placeTypes.put("Kreis", "district");
		placeTypes.put("Ort", "city");
		placeTypes.put("Stadt", "city");
		placeTypes.put("Region", "region");
		placeTypes.put("Landschaft", "region");
		placeTypes.put("Fluss", "river");
		placeTypes.put("Meer", "ocean");
		placeTypes.put("See", "lake");
		placeTypes.put("Staat", "country");
		placeTypes.put("Bundesland", "state");
		PLACE_TYPES = Collections.unmodifiableMap(placeTypes);
	}
	
	public final static Map<String,String> COUNTRIES;
	static {
		Map<String, String> countries = new HashMap<String,String>();
		countries.put("ger", "deu");
		countries.put("fre", "fra");
		COUNTRIES = Collections.unmodifiableMap(countries);
	}
	
	public final static List<String> ROOT_IDS = Arrays.asList(
		//"xTopLand", // Klassische Archäologie -> Topographie -> Länder mit Gebieten und Orten
		//"3.00.01", // Iberische Halbinsel -> Topographie
		"zTopogEuropMitteDeuts" // Topograhpie
		//"4.02" // Thesaurus Eurasien-Abteilung -> Regionen/Länder/Orte
	);
	
	private WebResource api;
	
	private IdGenerator idGenerator;
	
	private Set<String> types = new HashSet<String>();

	private PlaceRepository placeDao;
	
	public ZenonHarvester() {
		ClientConfig config = new DefaultClientConfig();
		config.getClasses().add(JacksonJsonProvider.class);
		Client client = Client.create(config);
		client.setConnectTimeout(5000);
		client.setReadTimeout(5000);
		api = client.resource(RESOURCE_URI);
	}
	
	@Override
	public void harvest(Date date) {
		
		for (String rootId : ROOT_IDS) {
			createPlacesRecursively(rootId, null, null, 0);
		}
		
	}

	@Override
	public void close() {
		logger.debug("types: {}", types);
		// nothing to do
	}

	@Override
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	private void createPlacesRecursively(String id, Place parent, String type, int level) {
		
		logger.debug("creating place with id {}", id);

		Builder builder = api.path("resource/" + id + "/").accept(MediaType.APPLICATION_JSON_TYPE);
		
		ObjectNode placeNode = null;	
		try {
			placeNode = builder.get(ObjectNode.class);
		} catch (Exception e) {
			try {
				placeNode = builder.get(ObjectNode.class);
			} catch (Exception e2) {
				logger.warn("Unable to resolve resource " + id +". Skipping ...", e2);
				return;
			}
		}
		
		if (placeNode == null) {
			logger.warn("Unable to parse JSON for resource {}. Skipping ...", id);
			return;
		}
		
		Place place = new Place();
		
		place.setId(idGenerator.generate(place));
		
		if (type != null) place.setType(type);
		
		// ID
		JsonNode controlNodes = placeNode.get("data").get("marc:controlfield");
		for (JsonNode controlNode : jsonWrap(controlNodes)) {			
			String tag = controlNode.get("@tag").asText();		
			if ("001".equals(tag)) {
				Identifier zenonId = new Identifier(controlNode.get("#text").asText(), "zenon-systemnr");
				place.addIdentifier(zenonId);
			}			
		}

		List<Place> children = new ArrayList<Place>();
		Set<String> uniqueNames = new HashSet<String>();
		JsonNode marcNodes = placeNode.get("data").get("marc:datafield");
		for (JsonNode marcNode : jsonWrap(marcNodes)) {
			
			String tag = marcNode.get("@tag").asText();

			// inline children
			if ("552".equals(tag)) {
				Place child = new Place();
				String text = marcNode.get("marc:subfield").get("#text").asText();
				if (text != null && !text.isEmpty()) {
					PlaceName name = new PlaceName(text);
					child.setPrefName(name);
					child.setId(idGenerator.generate(child));
					children.add(child);
					logger.debug("added inline child to queue: {}", child);
				}
			
			// place names
			} else if ("551".equals(tag)) {
				PlaceName name = new PlaceName();
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : jsonWrap(marcSubNodes)) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("a".equals(code)) {
						name.setTitle(text);
					} else if ("9".equals(code)) {
						if (COUNTRIES.containsKey(text))
							text = COUNTRIES.get(text);
						name.setLanguage(text);
					}
				}
				if (name.getTitle() != null && !name.getTitle().isEmpty() 
						&& !uniqueNames.contains(name.getTitle())) {
					if (place.getPrefName() == null) place.setPrefName(name);
					else place.addName(name);
					uniqueNames.add(name.getTitle());
				}
				
			} else if ("558".equals(tag)) {
				PlaceName name = new PlaceName();
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : jsonWrap(marcSubNodes)) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("a".equals(code)) {
						name.setTitle(text);
					} else if ("9".equals(code)) {
						if (COUNTRIES.containsKey(text))
							text = COUNTRIES.get(text);
						name.setLanguage(text);
					}
				}
				if (name.getTitle() != null && !name.getTitle().isEmpty() 
						&& !uniqueNames.contains(name.getTitle())) {
					if (place.getPrefName() == null) place.setPrefName(name);
					else place.addName(name);
					uniqueNames.add(name.getTitle());
				}
			
			// IDs
			} else if ("034".equals(tag)) {
				Identifier identifier = new Identifier();
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : jsonWrap(marcSubNodes)) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("0".equals(code)) {
						identifier.setValue(text);
					} else if ("2".equals(code)) {
						identifier.setContext(text);
					}
				}
				if (identifier.getValue() != null && !identifier.getValue().isEmpty()) {
					if ("geonames".equals(identifier.getContext())) {
						Link link = new Link();
						link.setObject("http://sws.geonames.org/" + identifier.getValue());
						link.setPredicate("owl:sameAs");
						place.addLink(link);
					}
					place.addIdentifier(identifier);
				}
			
			// Type
			} else if ("563".equals(tag)) {
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : jsonWrap(marcSubNodes)) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("a".equals(code)) {
						place.setType(PLACE_TYPES.get(text));
						types.add(text);
					}
				}
				
			} else if ("555".equals(tag)) {
				Comment comment = new Comment();
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : jsonWrap(marcSubNodes)) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("a".equals(code)) {
						comment.setText(text);
					} else if ("9".equals(code)) {
						if (COUNTRIES.containsKey(text))
							text = COUNTRIES.get(text);
						comment.setLanguage(text);
					}
				}
			} else if ("591".equals(tag)) {
				JsonNode marcSubNodes = marcNode.get("marc:subfield");
				for (JsonNode marcSubNode : jsonWrap(marcSubNodes)) {
					String code = marcSubNode.get("@code").asText();
					String text = marcSubNode.get("#text").asText();
					if ("a".equals(code)) {
						place.addTag(text);
					} else if ("b".equals(code)) {
						place.addTag(text);
					}
				}
			}
			
		}
		
		if (level == 1 && place.getType() == null) {
			place.setType("continent");
		} else if (level == 3 && place.getType() == null) {
			place.setType("country");
		}
		
		Identifier zenonId = new Identifier();
		zenonId.setContext("zenon-thesaurus");
		zenonId.setValue(id);
		place.addIdentifier(zenonId);
		
		Place nextParent = place;
		boolean skip = false;
		String nextType = null;
		for (String skipTitle : SKIP_TITLES) {
			if (place.getPrefName().getTitle().matches(skipTitle)) {
				skip = true;
				nextParent = parent;
				nextType = PLACE_TYPES.get(skipTitle);
				logger.debug("found skipTitle {}, adding Type {} to children", skipTitle, PLACE_TYPES.get(skipTitle));
				break;
			}
		}
		if (!skip) {
			if (parent != null) {
				place.setParent(parent.getId());
				parent.setChildren(parent.getChildren()+1);
			}
			place.setNeedsReview(true);
			placeDao.save(place);
			logger.debug("added place to queue: {}", place);
		}
		
		nextParent.setChildren(nextParent.getChildren() + children.size());
		for (Place child : children) {
			child.setParent(nextParent.getId());
			child.setType(nextType);
			place.setNeedsReview(true);
			placeDao.save(place);
			logger.debug("added child to queue: {}", child);
		}
		
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
			try {
				childNodes = builder.get(ObjectNode.class).get("data");
			} catch (Exception e2) {
				logger.warn("Unable to get children for resource " + id +". Skipping ...", e2);
				return;
			}
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
				createPlacesRecursively(childId, nextParent, nextType, level + 1);
			}
			
		}
		
	}

	// wrap single objects in array in order to allow iteration
	private JsonNode jsonWrap(JsonNode jsonNode) {
		if (jsonNode.isArray()) return jsonNode;
		else {
			ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
			arrayNode.add(jsonNode);
			return arrayNode;
		}
	}

	@Override
	public void setPlaceRepository(PlaceRepository placeRepository) {
		placeDao = placeRepository;
	}

}
