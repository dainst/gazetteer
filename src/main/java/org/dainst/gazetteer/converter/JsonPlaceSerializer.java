package org.dainst.gazetteer.converter;

import org.dainst.gazetteer.domain.Comment;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Link;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		
		ObjectNode placeNode = mapper.createObjectNode();
		placeNode.put("@id", baseUri + "place/" + place.getId());
		placeNode.put("gazId", place.getId());
		if (place.getType() != null && !place.getType().isEmpty())
			placeNode.put("type", place.getType());
		if (place.getThesaurus() != null && !place.getThesaurus().isEmpty())
			placeNode.put("thesaurus", place.getThesaurus());
		
		// preferred name
		if (place.getPrefName() != null) {
			ObjectNode prefNameNode = mapper.createObjectNode();
			prefNameNode.put("title", place.getPrefName().getTitle());
			if (place.getPrefName().getLanguage() != null)
				prefNameNode.put("language", place.getPrefName().getLanguage());
			placeNode.put("prefName", prefNameNode);
		}
		
		// other names
		if (!place.getNames().isEmpty()) {
			ArrayNode namesNode = mapper.createArrayNode();
			for (PlaceName name : place.getNames()) {
				ObjectNode nameNode = mapper.createObjectNode();
				nameNode.put("title", name.getTitle());
				if (name.getLanguage() != null) nameNode.put("language", name.getLanguage());
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
		
		// children
		if (!place.getChildren().isEmpty()) {
			ArrayNode childrenNode = mapper.createArrayNode();
			for (String childId : place.getChildren()) {
				childrenNode.add(baseUri + "place/" + childId);
			}
			placeNode.put("children", childrenNode);
		}
		
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
			for (Tag tag : place.getTags()) {
				ObjectNode tagNode = mapper.createObjectNode();
				tagNode.put("text", tag.getText());
				tagNode.put("language", tag.getLanguage());
				tagsNode.add(tagNode);
			}
			placeNode.put("tags", tagsNode);
		}
		
		try {
			return mapper.writeValueAsString(placeNode);
		} catch (Exception e) {
			logger.error("Unable to serialize place in JSON.", e);
			return "";
		}
		
	}
	
}
