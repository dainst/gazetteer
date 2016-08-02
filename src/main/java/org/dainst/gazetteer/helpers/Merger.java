package org.dainst.gazetteer.helpers;

import java.util.Set;

import org.dainst.gazetteer.domain.Place;

public interface Merger {

	public Set<Place> merge(Place place1, Place place2);
	
}
