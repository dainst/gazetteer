package org.dainst.gazetteer.converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class JsonPlaceDeserializer {
	
	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.JsonPlaceDeserializer");
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Autowired
	private PlaceDao placeDao;

	public Place deserialize(InputStream jsonStream) throws DeserializeException {
		
		try {
		
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(jsonStream, ObjectNode.class);
			
			Place place = getPlaceForNode(objectNode.get("@id"));
			
			if (place == null) {
				place = new Place();
				String placeIdString = objectNode.get("@id").asText().replace(baseUri + "place/", "");
				logger.debug("read id from uri: {}", placeIdString);
				place.setId(Long.valueOf(placeIdString).longValue());
			}
			
			// set parent place from URI 
			JsonNode parentNode = objectNode.get("parent");
			if (parentNode != null) {
				Place parent = getPlaceForNode(parentNode);
				if (parent != null) place.setParent(parent);
			}
			
			// set child places from URIs
			JsonNode childrenNode = objectNode.get("children");
			if (childrenNode != null) for (JsonNode childNode : childrenNode) {
				Place child = getPlaceForNode(childNode);
				if (child != null) place.addChild(child);
			}
			

			List<PlaceName> names = new ArrayList<PlaceName>(place.getNames());
			
			// create or update name objects
			JsonNode namesNode = objectNode.get("names");
			if (namesNode != null) for (JsonNode nameNode : namesNode) {
				PlaceName name = null;
				for (PlaceName n : names)
					if (nameNode.has("id") && n.getId() == nameNode.get("id").asLong())
						name = n;
				if (name == null) {
					name = new PlaceName();
					place.addName(name);
				}
				JsonNode languageNode = nameNode.get("language");
				if (languageNode == null) 
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"language\" has to be set.");
				JsonNode titleNode = nameNode.get("title");
				if (titleNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"title\" has to be set.");
				name.setLanguage(languageNode.asText());
				name.setTitle(titleNode.asText());
				names.remove(name);
				logger.debug("updated placename: {}", name.getId());
			}
			
			// delete old name objects that are not referenced in the json
			for (PlaceName name : names) {
				logger.debug("removing placename: {}", name.getId());
				place.removeName(name);
			}
			
			Set<Location> locations = new HashSet<Location>(place.getLocations());
			
			// create location objects
			JsonNode locationsNode = objectNode.get("locations");
			if (locationsNode != null) for (JsonNode locationNode : locationsNode) {
				Location location = null;
				for (Location l : locations)
					if (locationNode.has("id") && l.getId() == locationNode.get("id").asLong())
						location = l;
				if (location == null) {
					location = new Location();					
					place.addLocation(location);
				}
				JsonNode coordinatesNode = locationNode.get("coordinates");
				if (coordinatesNode == null)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" has to be set.");
				JsonNode latNode = coordinatesNode.get(0);
				if (latNode == null)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
				JsonNode longNode = coordinatesNode.get(1);
				if (longNode == null)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
	
				double lat = latNode.asDouble(1000);
				double lng = longNode.asDouble(1000);
				if (lat > 90 || lat < -90 || lng > 180 || lng < -180)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
				
				location.setCoordinates(new double[]{lat, lng});
				locations.remove(location);
				logger.debug("updated location: {}", location.getId());
				
			}
			
			// delete old name objects that are not referenced in the json
			for (Location location : locations) {
				logger.debug("removing location: {}", location.getId());
				place.removeLocation(location);
			}
					
			return place;
			
		} catch (Exception e) {
			logger.error("Unable to deserialize json to place object", e);
			throw new DeserializeException("Unable to deserialize json to place object", e);
		}
		
	}
	
	private Place getPlaceForNode(JsonNode node) throws InvalidIdException {
		
		String placeUri = node.asText();
		
		if (placeUri.startsWith(baseUri)) {
			String placeIdString = placeUri.replace(baseUri + "place/", "");
			try {
				long placeId = Long.valueOf(placeIdString).longValue();
				Place place = placeDao.get(placeId);
				return place;
			} catch (NumberFormatException e) {
				throw new InvalidIdException("Invalid id: " + placeIdString, e);
			}
		} else {
			return placeDao.getPlaceByUri(placeUri);
		}
		
	}
	
	private static class InvalidIdException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public InvalidIdException(String msg, Throwable cause) {
			super(msg, cause);
		}
		
	}
	
	public static class DeserializeException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public DeserializeException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}
	
}
