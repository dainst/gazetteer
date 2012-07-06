package org.dainst.gazetteer.converter;

import java.io.IOException;
import java.nio.charset.Charset;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Description;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonPlaceMessageConverter extends AbstractHttpMessageConverter<Place> {
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Autowired
	private PlaceDao placeDao;

	public JsonPlaceMessageConverter() {
		super(new MediaType("application","json", Charset.forName("UTF-8")));
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return Place.class.isAssignableFrom(clazz);
	}

	@Override
	protected Place readInternal(Class<? extends Place> clazz,
			HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		
		try {
		
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(inputMessage.getBody(), ObjectNode.class);
			
			Place place = new Place();
			
			// set parent place from URI 
			JsonNode parentNode = objectNode.get("parent");
			if (parentNode != null) {
				try {
					Place parent = getPlaceForNode(parentNode);
					if (parent != null) place.setParent(parent);
				} catch (InvalidIdException e) {
					throw new HttpMessageNotReadableException("Invalid parent id in json object.", e);
				}
			}
			
			// set child places from URIs
			JsonNode childrenNode = objectNode.get("children");
			if (childrenNode != null) for (JsonNode childNode : childrenNode) {
				try {
					Place child = getPlaceForNode(childNode);
					if (child != null) place.addChild(child);
				} catch (InvalidIdException e) {
					throw new HttpMessageNotReadableException("Invalid child id in json object.", e);
				}
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
			
			// create descriptions
			JsonNode descriptionsNode = objectNode.get("descriptions");
			if (descriptionsNode != null) for (JsonNode descriptionNode : descriptionsNode) {
				JsonNode descriptionTextNode = descriptionNode.get("description");
				if (descriptionTextNode == null)
					throw new HttpMessageNotReadableException("Invalid description object. Attribute \"description\" has to be set.");
				JsonNode languageNode = descriptionNode.get("language");
				if (languageNode == null) 
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"language\" has to be set.");
				Description description = new Description();
				description.setDescription(descriptionTextNode.asText());
				description.setLanguage(languageNode.asText());
				place.addDescription(description);
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
				
				// create descriptions for locations
				JsonNode locDescriptionsNode = locationNode.get("descriptions");
				if (descriptionsNode != null) for (JsonNode descriptionNode : locDescriptionsNode) {
					JsonNode descriptionTextNode = descriptionNode.get("description");
					if (descriptionTextNode == null)
						throw new HttpMessageNotReadableException("Invalid description object. Attribute \"description\" has to be set.");
					JsonNode languageNode = descriptionNode.get("language");
					if (languageNode == null) 
						throw new HttpMessageNotReadableException("Invalid name object. Attribute \"language\" has to be set.");
					Description description = new Description();
					description.setDescription(descriptionTextNode.asText());
					description.setLanguage(languageNode.asText());
					location.addDescription(description);
				}
				
				place.addLocation(location);
				
			}
					
			return place;
		
		} catch (HttpMessageNotReadableException e) {
			logger.error(e);
			throw e;
		}
		
	}

	@Override
	protected void writeInternal(Place place, HttpOutputMessage outputMessage)
			throws IOException, HttpMessageNotWritableException {
		
		throw new IllegalStateException("method writeInternal() is not implemented and should never be called.");
		
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

}
