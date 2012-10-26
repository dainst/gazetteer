package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.domain.Place;

public interface IdGenerator {
	
	public String generate(Place place);

}
