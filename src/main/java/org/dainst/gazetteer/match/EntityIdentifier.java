package org.dainst.gazetteer.match;

import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;

public interface EntityIdentifier {
	
	public void setPlaceRepository(PlaceRepository placeRepository);
	
	public List<Candidate> getCandidates(Place place);

}
