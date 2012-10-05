package org.dainst.gazetteer.helpers;

import java.util.List;

import org.dainst.gazetteer.dao.IdentifierDao;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.dao.PlaceDao.Query;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;

public class SimpleNameAndIdBasedEntityIdentifier implements EntityIdentifier {
	
	@Autowired
	PlaceDao placeDao;
	
	@Autowired
	IdentifierDao identifierDao;

	@Override
	public Place identify(Place place) {
		
		// identifier equality is a perfect match
		for (Identifier id : place.getIdentifiers()) {
			Identifier matchedId = identifierDao.getIdentifier(id.getValue(), id.getContext());
			if (matchedId != null) return matchedId.getPlace();
		}
		
		if ("continent".equals(place.getType())) {
			
			// we suppose that the names of continents are unique
			Query query = placeDao.new Query("SELECT FROM Place p JOIN p.names n "
					+ "WHERE p.type = :type AND n.name = :name");
			query.setParameter("type", "continent");
			query.setParameter("name", place.getNames().get(0).getTitle());
			List<Place> resultList = query.getResultList();
			if (resultList.size() == 1) return resultList.get(0);
			else return null;
		
		} else if ("country".equals(place.getType())) {

			// we suppose that the names of countries are unique
			Query query = placeDao.new Query("SELECT FROM Place p JOIN p.names n "
					+ "WHERE p.type = :type AND n.name = :name");
			query.setParameter("type", "country");
			query.setParameter("name", place.getNames().get(0).getTitle());
			List<Place> resultList = query.getResultList();
			if (resultList.size() == 1) return resultList.get(0);
			
		} else if ("country".equals(place.getType())) {

			// XXX we suppose that the names of cities in the same country are unique
			Query query = placeDao.new Query("SELECT FROM Place place JOIN p.names name "
					+ "JOIN place.parent parent JOIN parent.names parentName "
					+ "WHERE place.type = :type AND name.title = :name "
					+ "AND parent.type = :parent_type AND parentName.title = :parent_name");
			query.setParameter("type", "city");
			query.setParameter("name", place.getNames().get(0).getTitle());
			query.setParameter("parent_type", "country");
			query.setParameter("parent_name", place.getParent().getNames().get(0).getTitle());
			List<Place> resultList = query.getResultList();
			if (resultList.size() == 1) return resultList.get(0);
			
		}
		
		return null;
		
	}

}
