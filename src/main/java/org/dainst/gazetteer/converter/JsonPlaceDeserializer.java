package org.dainst.gazetteer.converter;

import java.io.InputStream;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
public class JsonPlaceDeserializer {
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Autowired
	private PlaceDao placeDao;

	public Place deserialize(InputStream jsonStream) throws DeserializeException {
		
		try {
		
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(jsonStream, ObjectNode.class);
			
			Place place = new Place();
			
			// set place id from uri
			String placeIdString = objectNode.get("@id").asText().replace(baseUri + "place/", "");
			place.setId(Long.valueOf(placeIdString).longValue());
			
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
			
			// create name objects
			JsonNode namesNode = objectNode.get("names");
			if (namesNode != null) for (JsonNode nameNode : namesNode) {
				JsonNode languageNode = nameNode.get("language");
				if (languageNode == null) 
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"language\" has to be set.");
				JsonNode titleNode = nameNode.get("title");
				if (titleNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"title\" has to be set.");
				PlaceName name = new PlaceName();
				name.setLanguage(languageNode.asText());
				name.setTitle(titleNode.asText());
				place.addName(name);
			}
			
			// create location objects
			JsonNode locationsNode = objectNode.get("locations");
			if (locationsNode != null) for (JsonNode locationNode : locationsNode) {
				
				JsonNode coordinatesNode = locationNode.get("coordinates");
				if (coordinatesNode == null)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" has to be set.");
				JsonNode latNode = coordinatesNode.get(0);
				if (latNode == null)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
				JsonNode longNode = coordinatesNode.get(0);
				if (longNode == null)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
	
				double lat = latNode.asDouble();
				double lng = longNode.asDouble();
				if (lat == 0 || lng == 0)
					throw new HttpMessageNotReadableException("Invalid location object. Attribute \"coordinates\" cannot be read.");
				Location location = new Location(lat, lng);
				
				place.addLocation(location);
				
			}
					
			return place;
			
		} catch (Exception e) {
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
