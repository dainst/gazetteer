package org.dainst.gazetteer.helpers;

import java.util.ArrayList;
import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;

public class GrandparentsHelper {
	
	private PlaceRepository placeDao;
	
	public GrandparentsHelper(PlaceRepository placeDao) {
		this.placeDao = placeDao;
	}
	
	public void updatePlaceGrandparents(Place place) {
		
		List<String> grandparentIds = new ArrayList<String>(place.getGrandparents());
		
		if (place.getParent() != null && !place.getParent().isEmpty())
			grandparentIds.add(place.getParent());
		
		updatePlaceGrandparents(place, grandparentIds);
	}
	
	private void updatePlaceGrandparents(Place place, List<String> grandparentIds) {

		List<String> placeGrandparentIds = new ArrayList<String>(grandparentIds);
		if (!placeGrandparentIds.isEmpty())
			placeGrandparentIds.remove(0);
		if (!place.getGrandparents().equals(placeGrandparentIds)) {
			place.setGrandparents(placeGrandparentIds);
			placeDao.save(place);
		}

		grandparentIds.add(0, place.getId());

		List<Place> children = placeDao.findByParentAndDeletedIsFalse(place.getId());
		for (Place child : children) {
			updatePlaceGrandparents(child, new ArrayList<String>(grandparentIds));
		}
	}
	
	public List<String> findGrandparentIds(Place place) {
		return findGrandparentIds(place, new ArrayList<String>(), true);
	}
	
	private List<String> findGrandparentIds(Place place, List<String> grandparentIds, boolean firstLevel) {
		
		if (place.getParent() != null && !place.getParent().isEmpty()) {
			Place parent = placeDao.findOne(place.getParent());
			if (parent != null) {
				if (!firstLevel)
					grandparentIds.add(parent.getId());
				grandparentIds = findGrandparentIds(parent, grandparentIds, false);
			}
		}
		
		return grandparentIds;
	}
}
