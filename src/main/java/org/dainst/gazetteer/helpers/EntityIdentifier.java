package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;

public interface EntityIdentifier {
	
	public Place identify(Place place, Thesaurus thesaurus);

}
