package org.dainst.gazetteer.helpers;

import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleMerger implements Merger {
	
	private static Logger logger = LoggerFactory.getLogger(SimpleMerger.class);
	
	private PlaceRepository placeRepository;
	
	private boolean newHasPriority = false;

	@Override
	public Place merge(Place place1, Place place2) {
		
		// data in place2 should take priority
		if (newHasPriority) {
			Place buf = place1;
			place1 = place2;
			place2 = buf;
		}
		
		Place result = new Place();
		
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
		
		result.setChildren(place1.getChildren() + place2.getChildren());
		
		if (place1.getPrefName() != null) {
			result.setPrefName(place1.getPrefName());
			if (!result.getPrefName().equals(place2.getPrefName()))
				result.addName(place2.getPrefName());
		} else {
			result.setPrefName(place2.getPrefName());
		}
		
		if (place1.getPrefLocation() != null) {
			result.setPrefLocation(place1.getPrefLocation());
			if (!result.getPrefLocation().equals(place2.getPrefLocation())
					&& place2.getPrefLocation() != null )
				result.addLocation(place2.getPrefLocation());
		} else {
			result.setPrefLocation(place2.getPrefLocation());
		}
		
		if (place1.getParent() != null && !place1.getParent().isEmpty())
			result.setParent(place1.getParent());
		else
			result.setParent(place2.getParent());
		
		if (place1.getType() != null && !place1.getType().isEmpty())
			result.setType(place1.getType());
		else
			result.setType(place2.getType());
		
		// the id is always determined by the first parameter of this function
		if (newHasPriority) result.setId(place2.getId());
		else result.setId(place1.getId());
		
		String oldId = place2.getId();
		if (newHasPriority) oldId = place1.getId();
		
		List<Place> children = getPlaceRepository().findByParent(oldId);
		logger.info("got {} children", children.size());
		for (Place child : children) {
			child.setParent(result.getId());
			placeRepository.save(child);
		}
		
		return result;
		
	}

	public PlaceRepository getPlaceRepository() {
		return placeRepository;
	}

	public void setPlaceRepository(PlaceRepository placeRepository) {
		this.placeRepository = placeRepository;
	}

	public boolean isNewHasPriority() {
		return newHasPriority;
	}

	public void setNewHasPriority(boolean newHasPriority) {
		this.newHasPriority = newHasPriority;
	}

}
