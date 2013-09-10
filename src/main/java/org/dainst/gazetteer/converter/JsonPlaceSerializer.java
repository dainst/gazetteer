package org.dainst.gazetteer.converter;

import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonPlaceSerializer {
	
	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.JsonPlaceSerializer");

	private String baseUri;
	
	private ObjectMapper mapper;
	
	public JsonPlaceSerializer(String baseUri) {
		this.baseUri = baseUri;
		mapper = new ObjectMapper();
	}
	
	public String serialize(Place place) {
		return serialize(place, 1);
	}
	
	public String serialize(Place place, int lod) {
		
		ObjectNode placeNode = mapper.createObjectNode();
		logger.debug("serializing: {}", place);
		placeNode.put("@id", baseUri + "place/" + place.getId());
		placeNode.put("gazId", place.getId());
		
		if (place.isDeleted()) {
			placeNode.put("deleted", true);
			try {
				return mapper.writeValueAsString(placeNode);
			} catch (Exception e) {
				logger.error("Unable to serialize place in JSON.", e);
				return "";
			}
		}
		
		if (place.getType() != null && !place.getType().isEmpty())
			placeNode.put("type", place.getType());
		
		// preferred name
		if (place.getPrefName() != null) {
			ObjectNode prefNameNode = mapper.createObjectNode();
			prefNameNode.put("title", place.getPrefName().getTitle());
			if (place.getPrefName().getLanguage() != null)
				prefNameNode.put("language", place.getPrefName().getLanguage());
			if (place.getPrefName().isAncient())
				prefNameNode.put("ancient", true);
			placeNode.put("prefName", prefNameNode);
		}
		
		// other names
		if (!place.getNames().isEmpty()) {
			ArrayNode namesNode = mapper.createArrayNode();
			for (PlaceName name : place.getNames()) {
				ObjectNode nameNode = mapper.createObjectNode();
				nameNode.put("title", name.getTitle());
				if (name.getLanguage() != null) 
					nameNode.put("language", name.getLanguage());
				if (name.isAncient())
					nameNode.put("ancient", true);
				namesNode.add(nameNode);
			}
			placeNode.put("names", namesNode);
		}
		
		// preferred location
		if (place.getPrefLocation() != null) {
			ObjectNode locationNode = mapper.createObjectNode();
			ArrayNode coordinatesNode = mapper.createArrayNode();
			coordinatesNode.add(place.getPrefLocation().getLng());
			coordinatesNode.add(place.getPrefLocation().getLat());
			locationNode.put("coordinates", coordinatesNode);
			locationNode.put("confidence", place.getPrefLocation().getConfidence());
			placeNode.put("prefLocation", locationNode);
		}
		
		// other locations
		if (!place.getLocations().isEmpty()) {
			ArrayNode locationsNode = mapper.createArrayNode();
			for (Location location : place.getLocations()) {
				ObjectNode locationNode = mapper.createObjectNode();
				ArrayNode coordinatesNode = mapper.createArrayNode();
				coordinatesNode.add(location.getLng());
				coordinatesNode.add(location.getLat());
				locationNode.put("coordinates", coordinatesNode);
				locationNode.put("confidence", location.getConfidence());
				locationsNode.add(locationNode);
			}
			placeNode.put("locations", locationsNode);
		}
		
		// identifiers
		if (!place.getIdentifiers().isEmpty()) {
			ArrayNode idsNode = mapper.createArrayNode();
			for (Identifier id : place.getIdentifiers()) {
				ObjectNode idNode = mapper.createObjectNode();
				idNode.put("value", id.getValue());
				idNode.put("context", id.getContext());
				idsNode.add(idNode);
			}
			placeNode.put("identifiers", idsNode);
		}
		
		// links
		if (!place.getLinks().isEmpty()) {
			ArrayNode linksNode = mapper.createArrayNode();
			for (Link link : place.getLinks()) {
				ObjectNode linkNode = mapper.createObjectNode();
				linkNode.put("object", link.getObject());
				linkNode.put("predicate", link.getPredicate());
				linksNode.add(linkNode);
			}
			placeNode.put("links", linksNode);
		}
		
		// parent
		if (place.getParent() != null && !place.getParent().isEmpty())
			placeNode.put("parent", baseUri + "place/" + place.getParent());
		
		// related places
		if (!place.getRelatedPlaces().isEmpty()) {
			ArrayNode relatedPlacesNode = mapper.createArrayNode();
			for (String relatedPlaceId : place.getRelatedPlaces()) {
				relatedPlacesNode.add(baseUri + "place/" + relatedPlaceId);
			}
			placeNode.put("relatedPlaces", relatedPlacesNode);
		}
		
		// comments
		if (!place.getComments().isEmpty()) {
			ArrayNode commentsNode = mapper.createArrayNode();
			for (Comment comment : place.getComments()) {
				ObjectNode commentNode = mapper.createObjectNode();
				commentNode.put("text", comment.getText());
				commentNode.put("language", comment.getLanguage());
				commentsNode.add(commentNode);
			}
			placeNode.put("comments", commentsNode);
		}
		
		// tags
		if (!place.getTags().isEmpty()) {
			ArrayNode tagsNode = mapper.createArrayNode();
			for (String tag : place.getTags()) {
				tagsNode.add(tag);
			}
			placeNode.put("tags", tagsNode);
		}
		
		// reisestipendium content		
		logger.debug("serializing reisestipendium content?");
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.debug("user: {}", principal);
		if (principal instanceof User) {
			User user = (User) principal;
			if (user.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_REISESTIPENDIUM"))) {
				
				logger.debug("serializing reisestipendium note");
				if (place.getNoteReisestipendium() != null && !place.getNoteReisestipendium().isEmpty()) {
					placeNode.put("noteReisestipendium", place.getNoteReisestipendium());
					logger.debug("serialized reisestipendium note");
				}

				if (!place.getCommentsReisestipendium().isEmpty()) {
					ArrayNode commentsNode = mapper.createArrayNode();
					for (Comment comment : place.getCommentsReisestipendium()) {
						ObjectNode commentNode = mapper.createObjectNode();
						commentNode.put("text", comment.getText());
						commentNode.put("user", comment.getUser());
						commentsNode.add(commentNode);
					}
					placeNode.put("commentsReisestipendium", commentsNode);
				}
				
			}
		}
		
		try {
			return mapper.writeValueAsString(placeNode);
		} catch (Exception e) {
			logger.error("Unable to serialize place in JSON.", e);
			return "";
		}
		
	}
	
}
