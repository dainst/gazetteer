package org.dainst.gazetteer.converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.Tag;
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

	public Place deserialize(InputStream jsonStream) throws HttpMessageNotReadableException {
		
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
			
			// set related places from URIs
			JsonNode relatedPlacesNode = objectNode.get("relatedPlaces");
			if (childrenNode != null) for (JsonNode relatedPlaceNode : relatedPlacesNode) {
				Place relatedPlace = getPlaceForNode(relatedPlaceNode);
				if (relatedPlace != null) place.addRelatedPlace(relatedPlace);
			}
			
			// set place type
			if (objectNode.has("type")) {
				place.setType(objectNode.get("type").asText());
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
				JsonNode titleNode = nameNode.get("title");
				if (titleNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"title\" has to be set.");
				if (languageNode != null) name.setLanguage(languageNode.asText());
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
			
			// create or update location objects
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
				
				if (locationNode.has("confidence")) {
					location.setConfidence(locationNode.get("confidence").asInt());
				}
				
				locations.remove(location);
				logger.debug("updated location: {}", location.getId());
				
			}
			
			// delete old location objects that are not referenced in the json
			for (Location location : locations) {
				logger.debug("removing location: {}", location.getId());
				place.removeLocation(location);
			}
			
			Set<Comment> comments = new HashSet<Comment>(place.getComments());
			
			// create or update comment objects
			JsonNode commentsNode = objectNode.get("comments");
			if (commentsNode != null) for (JsonNode commentNode : commentsNode) {
				Comment comment = null;
				for (Comment c : comments)
					if (commentNode.has("id") && c.getId() == commentNode.get("id").asLong())
						comment = c;
				if (comment == null) {
					comment = new Comment();					
					place.getComments().add(comment);
				}
				JsonNode languageNode = commentNode.get("language"); 
				JsonNode textNode = commentNode.get("text");
				if (textNode == null)
					throw new HttpMessageNotReadableException("Invalid comment object. Attribute \"text\" has to be set.");
				if (languageNode != null) comment.setLanguage(languageNode.asText());
				comment.setText(textNode.asText());
				comments.remove(comment);
				logger.debug("updated comment: {}", comment.getId());				
			}
			
			// delete old comment objects that are not referenced in the json
			for (Comment comment : comments) {
				logger.debug("removing comment: {}", comment.getId());
				place.getComments().remove(comment);
			}
			
			Set<Tag> tags = new HashSet<Tag>(place.getTags());
			
			// create or update tag objects
			JsonNode tagsNode = objectNode.get("tags");
			if (tagsNode != null) for (JsonNode tagNode : tagsNode) {
				Tag tag = null;
				for (Tag t : tags)
					if (tagNode.has("id") && t.getId() == tagNode.get("id").asLong())
						tag = t;
				if (tag == null) {
					tag = new Tag();					
					place.getTags().add(tag);
				}
				JsonNode languageNode = tagNode.get("language"); 
				JsonNode textNode = tagNode.get("text");
				if (textNode == null)
					throw new HttpMessageNotReadableException("Invalid tag object. Attribute \"text\" has to be set.");
				if (languageNode != null) tag.setLanguage(languageNode.asText());
				tag.setText(textNode.asText());
				tags.remove(tag);
				logger.debug("updated tag: {}", tag.getId());				
			}
			
			// delete old tag objects that are not referenced in the json
			for (Tag tag : tags) {
				logger.debug("removing tag: {}", tag.getId());
				place.getTags().remove(tag);
			}
			
			Set<Identifier> identifiers = new HashSet<Identifier>(place.getIdentifiers());
			
			// create or update identifier objects
			JsonNode identifiersNode = objectNode.get("identifiers");
			if (identifiersNode != null) for (JsonNode identifierNode : identifiersNode) {
				Identifier identifier = null;
				for (Identifier i : identifiers)
					if (identifierNode.has("id") && i.getId() == identifierNode.get("id").asLong())
						identifier = i;
				if (identifier == null) {
					identifier = new Identifier();					
					place.getIdentifiers().add(identifier);
				}
				JsonNode valueNode = identifierNode.get("value"); 
				JsonNode contextNode = identifierNode.get("context");
				if (valueNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"value\" has to be set.");
				if (contextNode != null) identifier.setContext(contextNode.asText());
				identifier.setValue(valueNode.asText());
				identifiers.remove(identifier);
				logger.debug("updated identifier: {}", identifier.getId());				
			}
			
			// delete old identifier objects that are not referenced in the json
			for (Identifier identifier : identifiers) {
				logger.debug("removing identifier: {}", identifier.getId());
				place.getIdentifiers().remove(identifier);
			}
					
			return place;
			
		} catch (Exception e) {
			String msg = "Unable to deserialize json to place object";
			logger.error(msg, e);
			throw new HttpMessageNotReadableException(e.getMessage(), e);
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
	
}
