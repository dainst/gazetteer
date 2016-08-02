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
	
	
	@Override
	public Set<Place> merge(Place place1, Place place2) {
		
		Set<Place> changedPlaces = new HashSet<Place>();
							
		place1.getComments().addAll(place2.getComments());
		place1.getCommentsReisestipendium().addAll(place2.getCommentsReisestipendium());
		place1.getIdentifiers().addAll(place2.getIdentifiers());
		place1.getLinks().addAll(place2.getLinks());
		place1.getLocations().addAll(place2.getLocations());
		place1.getNames().addAll(place2.getNames());
		place1.getTags().addAll(place2.getTags());
		place1.getProvenance().addAll(place2.getProvenance());
		place1.getTypes().addAll(place2.getTypes());
		place1.getGroupInternalData().addAll(place2.getGroupInternalData());
		
		place1.setChildren(place1.getChildren() + place2.getChildren());
		
		Set<String> relatedPlaces = new HashSet<String>();
		relatedPlaces.addAll(place1.getRelatedPlaces());
		relatedPlaces.addAll(place2.getRelatedPlaces());
		place1.setRelatedPlaces(new HashSet<String>());
		for (String relatedPlaceId : relatedPlaces) {
			if (relatedPlaceId != null) {
				Place relatedPlace = placeRepository.findOne(relatedPlaceId);
				if (relatedPlace != null && relatedPlace.getRelatedPlaces() != null
						&& (relatedPlace.getRelatedPlaces().contains(place1.getId()) || relatedPlace.getRelatedPlaces().contains(place2.getId())
						&& !relatedPlace.getId().equals(place1.getId()) && !relatedPlace.getId().equals(place2.getId()))) {
					place1.getRelatedPlaces().add(relatedPlaceId);
					relatedPlace.getRelatedPlaces().remove(place1.getId());
					relatedPlace.getRelatedPlaces().remove(place2.getId());
					relatedPlace.getRelatedPlaces().add(place1.getId());
					changedPlaces.add(relatedPlace);
				}
			}
		}
		
		if (place1.getPrefName() == null)
			place1.setPrefName(place2.getPrefName());
		else if (!place1.getPrefName().equals(place2.getPrefName()))
			place1.addName(place2.getPrefName());			
		
		if (place1.getPrefLocation() == null)
			place1.setPrefLocation(place2.getPrefLocation());
		else if (!place1.getPrefLocation().equals(place2.getPrefLocation()) && place2.getPrefLocation() != null)
			place1.addLocation(place2.getPrefLocation());
		
		// update parent id of children that belong to place 2
		List<Place> children = getPlaceRepository().findByParent(place2.getId());
		logger.info("got {} children", children.size());
		for (Place child : children) {
			Place changedChild = getFromPlaceSet(child.getId(), changedPlaces);
			if (changedChild != null)
				changedChild.setParent(place1.getId());
			else {
				child.setParent(place1.getId());
				changedPlaces.add(child);
			}
		}
		
		if (place1.getParent() == null || place1.getParent().isEmpty())
			place1.setParent(place2.getParent());
		else if (place2.getParent() != null && !place1.getParent().equals(place2.getParent())) {
			Place otherParent = getFromPlaceSet(place2.getParent(), changedPlaces);
			if (otherParent == null)
				otherParent = placeRepository.findOne(place2.getParent());
			if (otherParent != null) {
				place1.getRelatedPlaces().add(otherParent.getId());
				otherParent.getRelatedPlaces().add(place1.getId());
				changedPlaces.add(otherParent);
			}
		}
		
		String noteReisestipendium = "";
		if (place1.getNoteReisestipendium() != null && !place1.getNoteReisestipendium().isEmpty())
			noteReisestipendium += place1.getNoteReisestipendium();
		if (place2.getNoteReisestipendium() != null && !place2.getNoteReisestipendium().isEmpty()) {
			if (!noteReisestipendium.isEmpty())
				noteReisestipendium += "\n\n";
			noteReisestipendium += place2.getNoteReisestipendium(); 
		}
		if (!noteReisestipendium.isEmpty())
			place1.setNoteReisestipendium(noteReisestipendium);
		
		changedPlaces.add(place1);
		
		return changedPlaces;
	}

	public PlaceRepository getPlaceRepository() {
		return placeRepository;
	}

	public void setPlaceRepository(PlaceRepository placeRepository) {
		this.placeRepository = placeRepository;
	}
	
	private Place getFromPlaceSet(String id, Set<Place> places) {
		
		for (Place place : places) {
			if (place.getId().equals(id))
				return place;
		}
		
		return null;
	}
}
