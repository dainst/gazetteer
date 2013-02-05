package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.domain.Place;

public interface EntityIdentifier {
	
	public Place identify(Place place);

}
