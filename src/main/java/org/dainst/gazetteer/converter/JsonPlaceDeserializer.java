package org.dainst.gazetteer.converter;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.ThesaurusRepository;
import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.Tag;
import org.dainst.gazetteer.domain.Thesaurus;
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
	private PlaceRepository placeDao;
	
	@Autowired
	private ThesaurusRepository thesaurusDao;
	
	public Place deserializeLazily(InputStream jsonStream) throws InvalidIdException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(jsonStream, ObjectNode.class);
			
			Place place = null;
			if (objectNode.has("@id")) {
				place = getPlaceForNode(objectNode.get("@id"));
			}
			return place;
		} catch (Exception e) {
			throw new InvalidIdException("error while getting id from json", e);
		}
	}

	public Place deserialize(InputStream jsonStream) throws HttpMessageNotReadableException {
		
		try {
		
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode objectNode = mapper.readValue(jsonStream, ObjectNode.class);
			
			Place place = null;
			if (objectNode.has("@id")) {
				place = getPlaceForNode(objectNode.get("@id"));
			}
			
			if (place == null) {
				place = new Place();
				if (objectNode.has("@id")) {
					String placeIdString = objectNode.get("@id").asText().replace(baseUri + "place/", "");
					logger.debug("read id from uri: {}", placeIdString);
					place.setId(placeIdString);
				} else {
					place = placeDao.save(place);
					logger.debug("created new place with id: {}", place.getId());
				}
			}
			
			// set parent place from URI 
			JsonNode parentNode = objectNode.get("parent");
			if (parentNode != null) {
				Place parent = getPlaceForNode(parentNode);
				if (parent != null) {
					place.setParent(parent.getId());
					parent.addChild(place.getId());
				}
			}
			
			// set child places from URIs
			JsonNode childrenNode = objectNode.get("children");
			if (childrenNode != null) for (JsonNode childNode : childrenNode) {
				Place child = getPlaceForNode(childNode);
				if (child != null) {
					place.addChild(child.getId());
					child.setParent(place.getId());
				}
			}
			
			// set related places from URIs
			JsonNode relatedPlacesNode = objectNode.get("relatedPlaces");
			if (childrenNode != null) for (JsonNode relatedPlaceNode : relatedPlacesNode) {
				Place relatedPlace = getPlaceForNode(relatedPlaceNode);
				if (relatedPlace != null) {
					place.addRelatedPlace(relatedPlace.getId());
				}
			}
			
			// set place type
			if (objectNode.has("type")) {
				place.setType(objectNode.get("type").asText());
			}
			
			// set thesaurus
			if (objectNode.has("thesaurus")) {
				Thesaurus thesaurus = thesaurusDao.getThesaurusByKey(objectNode.get("thesaurus").asText());
				if (thesaurus == null)
					throw new HttpMessageNotReadableException("Invalid thesaurus key. Attribute \"title\" has to be set.");
				place.setThesaurus(thesaurus.getKey());
			}
			
			// update name objects
			List<PlaceName> names = new ArrayList<PlaceName>();
			JsonNode namesNode = objectNode.get("names");
			if (namesNode != null) for (JsonNode nameNode : namesNode) {
				PlaceName name = new PlaceName();
				names.add(name);				
				JsonNode languageNode = nameNode.get("language"); 
				JsonNode titleNode = nameNode.get("title");
				if (titleNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"title\" has to be set.");
				if (languageNode != null) name.setLanguage(languageNode.asText());
				name.setTitle(titleNode.asText());
				logger.debug("updated placename: {}", name);
			}
			place.setNames(names);
			
			// update location objects
			Set<Location> locations = new HashSet<Location>();
			JsonNode locationsNode = objectNode.get("locations");
			if (locationsNode != null) for (JsonNode locationNode : locationsNode) {
				Location location = new Location();					
				locations.add(location);
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
				
				logger.debug("updated location: {}", location);
				
			}
			place.setLocations(locations);
			
			// update comment objects			
			Set<Comment> comments = new HashSet<Comment>();
			JsonNode commentsNode = objectNode.get("comments");
			if (commentsNode != null) for (JsonNode commentNode : commentsNode) {
				Comment comment = new Comment();					
				comments.add(comment);
				JsonNode languageNode = commentNode.get("language"); 
				JsonNode textNode = commentNode.get("text");
				if (textNode == null)
					throw new HttpMessageNotReadableException("Invalid comment object. Attribute \"text\" has to be set.");
				if (languageNode != null) comment.setLanguage(languageNode.asText());
				comment.setText(textNode.asText());
				logger.debug("updated comment: {}", comment);				
			}
			place.setComments(comments);
			
			// update tag objects			
			Set<Tag> tags = new HashSet<Tag>();
			JsonNode tagsNode = objectNode.get("tags");
			if (tagsNode != null) for (JsonNode tagNode : tagsNode) {
				Tag tag = new Tag();					
				tags.add(tag);
				JsonNode languageNode = tagNode.get("language"); 
				JsonNode textNode = tagNode.get("text");
				if (textNode == null)
					throw new HttpMessageNotReadableException("Invalid tag object. Attribute \"text\" has to be set.");
				if (languageNode != null) tag.setLanguage(languageNode.asText());
				tag.setText(textNode.asText());
				logger.debug("updated tag: {}", tag);				
			}
			place.setTags(tags);
			
			// update identifier objects			
			Set<Identifier> identifiers = new HashSet<Identifier>();
			JsonNode identifiersNode = objectNode.get("identifiers");
			if (identifiersNode != null) for (JsonNode identifierNode : identifiersNode) {
				Identifier identifier = new Identifier();					
				identifiers.add(identifier);
				JsonNode valueNode = identifierNode.get("value"); 
				JsonNode contextNode = identifierNode.get("context");
				if (valueNode == null)
					throw new HttpMessageNotReadableException("Invalid name object. Attribute \"value\" has to be set.");
				if (contextNode != null) identifier.setContext(contextNode.asText());
				identifier.setValue(valueNode.asText());
				logger.debug("updated identifier: {}", identifier);				
			}
			place.setIdentifiers(identifiers);
					
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
				Place place = placeDao.findOne(placeIdString);
				return place;
			} catch (NumberFormatException e) {
				throw new InvalidIdException("Invalid id: " + placeIdString, e);
			}
		} else {
			return placeDao.getByLinksObjectAndLinksPredicate(placeUri, "owl:sameAs");
		}
		
	}
	
	private static class InvalidIdException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public InvalidIdException(String msg, Throwable cause) {
			super(msg, cause);
		}
		
	}
	
}
