package org.dainst.gazetteer.helpers;

import java.util.List;

import org.dainst.gazetteer.dao.IdentifierDao;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleNameAndIdBasedEntityIdentifier implements EntityIdentifier {
	
	private static Logger logger = LoggerFactory.getLogger(SimpleNameAndIdBasedEntityIdentifier.class);
	
	@Autowired
	PlaceDao placeDao;
	
	@Autowired
	IdentifierDao identifierDao;

	@Override
	public Place identify(Place place, Thesaurus thesaurus) {
		
		// identifier equality is a perfect match
		for (Identifier id : place.getIdentifiers()) {
			Identifier matchedId = identifierDao.getIdentifier(id.getValue(), id.getContext());
			if (matchedId != null) {
				logger.debug("matched id: " + matchedId.getValue());
				return matchedId.getPlace();
			}
		}
		
		if ("continent".equals(place.getType())) {
			
			// we suppose that the names of continents are unique
			List<Place> resultList = placeDao.getPlacesByNameAndType(
					place.getNames().get(0).getTitle(), "continent");
			logger.debug("matched continents: " + resultList.size());
			if (resultList.size() == 1) return resultList.get(0);
			else return null;
		
		} else if ("country".equals(place.getType())) {

			// we suppose that the names of countries are unique
			List<Place> resultList = placeDao.getPlacesByNameAndType(
					place.getNames().get(0).getTitle(), "country");
			logger.debug("matched countries: " + resultList.size());
			if (resultList.size() == 1) return resultList.get(0);
			else return null;
			
		} else if ("city".equals(place.getType())) {

			// XXX we suppose that the names of cities in the same country are unique
			List<Place> resultList = placeDao.getPlacesByNameAndTypeIncludingParent(
					place.getNames().get(0).getTitle(), "city",
					place.getParent().getNames().get(0).getTitle(), "country");
			logger.debug("matched cities: " + resultList.size());
			if (resultList.size() == 1) return resultList.get(0);
			else return null;
			
		}
		
		return null;
		
	}

}
