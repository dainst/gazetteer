package org.dainst.gazetteer.helpers;

import java.util.ArrayList;
import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AncestorsHelper {
	
	private PlaceRepository placeDao;
	
	private static Logger logger = LoggerFactory.getLogger(AncestorsHelper.class);
	
	public AncestorsHelper(PlaceRepository placeDao) {
		this.placeDao = placeDao;
	}
	
	public void updateAncestors(Place place) {
		
		List<String> ancestorIds = new ArrayList<String>(place.getAncestors());
		
		if (place.getParent() != null && !place.getParent().isEmpty())
			ancestorIds.add(place.getParent());
		
		updateAncestors(place, ancestorIds);
	}
	
	private void updateAncestors(Place place, List<String> ancestorIds) {

		List<String> placeAncestorIds = new ArrayList<String>(ancestorIds);
		if (!placeAncestorIds.isEmpty())
			placeAncestorIds.remove(0);
		if (!place.getAncestors().equals(placeAncestorIds)) {
			place.setAncestors(placeAncestorIds);
			placeDao.save(place);
		}

		ancestorIds.add(0, place.getId());

		List<Place> children = placeDao.findByParentAndDeletedIsFalse(place.getId());
		for (Place child : children) {
			updateAncestors(child, new ArrayList<String>(ancestorIds));
		}
	}
	
	public List<String> findAncestorIds(Place place) {
		return findAncestorIds(place, new ArrayList<String>(), true);
	}
	
	private List<String> findAncestorIds(Place place, List<String> ancestorIds, boolean firstLevel) {
		
		if (place.getParent() != null && place.getParent().equals(place.getId())) {
			logger.warn("Place " + place.getId() + " is its own parent place!");
			return ancestorIds;
		}
		
		if (place.getParent() != null && !place.getParent().isEmpty()) {
			Place parent = placeDao.findById(place.getParent()).orElse(null);
			if (parent != null) {
				if (!firstLevel)
					ancestorIds.add(parent.getId());
				ancestorIds = findAncestorIds(parent, ancestorIds, false);
			}
		}
		
		return ancestorIds;
	}
}
