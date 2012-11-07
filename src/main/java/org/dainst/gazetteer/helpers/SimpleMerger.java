package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.domain.Place;

public class SimpleMerger implements Merger {

	@Override
	public Place merge(Place place1, Place place2) {
		
		Place result = new Place();
		
		result.getChildren().addAll(place1.getChildren());
		result.getChildren().addAll(place2.getChildren());
		result.getComments().addAll(place1.getComments());
		result.getComments().addAll(place2.getComments());
		result.getIdentifiers().addAll(place1.getIdentifiers());
		result.getIdentifiers().addAll(place2.getIdentifiers());
		result.getLinks().addAll(place1.getLinks());
		result.getLinks().addAll(place2.getLinks());
		result.getLocations().addAll(place1.getLocations());
		result.getLocations().addAll(place2.getLocations());
		result.getNames().addAll(place1.getNames());
		result.getNames().addAll(place2.getNames());
		result.getRelatedPlaces().addAll(place1.getRelatedPlaces());
		result.getRelatedPlaces().addAll(place2.getRelatedPlaces());
		result.getTags().addAll(place1.getTags());
		result.getTags().addAll(place2.getTags());
		
		result.setThesaurus(place1.getThesaurus());
		
		result.setPrefName(place1.getPrefName());
		if (!result.getPrefName().equals(place2.getPrefName()))
			result.addName(place2.getPrefName());
		
		if (place1.getParent() != null && place1.getParent().isEmpty())
			result.setParent(place1.getParent());
		else
			result.setParent(place2.getParent());
		
		if (place1.getType() != null && !place1.getType().isEmpty())
			result.setType(place1.getType());
		else
			result.setType(place2.getType());
		
		return result;
		
	}

}
