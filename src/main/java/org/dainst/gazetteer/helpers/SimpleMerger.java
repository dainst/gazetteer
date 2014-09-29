package org.dainst.gazetteer.helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		
		result.setRecordGroupId(place1.getRecordGroupId());
		
		result.getComments().addAll(place1.getComments());
		result.getComments().addAll(place2.getComments());
		result.getCommentsReisestipendium().addAll(place1.getCommentsReisestipendium());
		result.getCommentsReisestipendium().addAll(place2.getCommentsReisestipendium());
		result.getIdentifiers().addAll(place1.getIdentifiers());
		result.getIdentifiers().addAll(place2.getIdentifiers());
		result.getLinks().addAll(place1.getLinks());
		result.getLinks().addAll(place2.getLinks());
		result.getLocations().addAll(place1.getLocations());
		result.getLocations().addAll(place2.getLocations());
		result.getNames().addAll(place1.getNames());
		result.getNames().addAll(place2.getNames());
		result.getTags().addAll(place1.getTags());
		result.getTags().addAll(place2.getTags());
		result.getProvenance().addAll(place1.getProvenance());
		result.getProvenance().addAll(place2.getProvenance());
		result.getTypes().addAll(place1.getTypes());
		result.getTypes().addAll(place2.getTypes());
		
		result.setChildren(place1.getChildren() + place2.getChildren());
		
		Set<String> relatedPlaces = new HashSet<String>();
		relatedPlaces.addAll(place1.getRelatedPlaces());
		relatedPlaces.addAll(place2.getRelatedPlaces());
		for (String relatedPlaceId : relatedPlaces) {
			if (relatedPlaceId != null) {
				Place relatedPlace = placeRepository.findOne(relatedPlaceId);
				if (relatedPlace != null && relatedPlace.getRelatedPlaces() != null
						&& (relatedPlace.getRelatedPlaces().contains(place1.getId()) || relatedPlace.getRelatedPlaces().contains(place2.getId())
						&& !relatedPlace.getId().equals(place1.getId()) && !relatedPlace.getId().equals(place2.getId())))
					result.getRelatedPlaces().add(relatedPlaceId);
			}
		}
		
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
		
		// the id is always determined by the first parameter of this function
		if (newHasPriority) result.setId(place2.getId());
		else result.setId(place1.getId());
		
		String oldId = place2.getId();
		if (newHasPriority) oldId = place1.getId();
		
		// update parent id of children
		List<Place> children = getPlaceRepository().findByParent(oldId);
		logger.info("got {} children", children.size());
		for (Place child : children) {
			child.setParent(result.getId());
			placeRepository.save(child);
		}
		
		if (place1.getNoteReisestipendium() != null && !place1.getNoteReisestipendium().isEmpty())
			result.setNoteReisestipendium(place1.getNoteReisestipendium());
		else if (place2.getNoteReisestipendium() != null && !place2.getNoteReisestipendium().isEmpty())
			result.setNoteReisestipendium(place2.getNoteReisestipendium());
		
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
