package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.domain.Place;

public interface Merger {

	public Place merge(Place place1, Place place2);
	
}
