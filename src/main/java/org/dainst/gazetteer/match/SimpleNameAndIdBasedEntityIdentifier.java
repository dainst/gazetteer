package org.dainst.gazetteer.match;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleNameAndIdBasedEntityIdentifier implements EntityIdentifier {
	
	private static final Logger logger = LoggerFactory.getLogger(SimpleNameAndIdBasedEntityIdentifier.class);
	
	private PlaceRepository placeDao;

	@Override
	public List<Candidate> getCandidates(Place place) {
		
		List<Candidate> candidates = new ArrayList<Candidate>();
		
		// identifier equality is a perfect match
		for (Identifier id : place.getIdentifiers()) {
			Place matchedPlace = null;
			if ("gazetteer".equals(id.getContext())) {
				matchedPlace = placeDao.findById(id.getValue()).orElse(null);
			} else if (!"zenon-thesaurus".equals(id.getContext())) {
				matchedPlace = placeDao.findByIdsAndNeedsReviewAndIdNot(
					id, false, place.getId());
			}
			if (matchedPlace != null && id.getValue() != null) {
				logger.debug("matched id: " + id);
				candidates.add(new Candidate(place, matchedPlace, 1));				
				logger.debug("returning candidates: {}", candidates);
				return candidates;
			}
		}
		
		if (place.getTypes().contains("continent")) {
			
			// we suppose that the names of continents are unique
			List<Place> resultList = placeDao.findByPrefNameTitleAndTypesAndNeedsReviewAndIdNot(
					place.getPrefName().getTitle(), "continent", false, place.getId());
			logger.debug("matched continents: " + resultList.size());
			if (resultList.size() == 1) {
				candidates.add(new Candidate(place, resultList.get(0), 1));
				return candidates;
			}
		
		} else if (place.getTypes().contains("administrative-unit")) {

			// we suppose that the names of countries are unique
			List<Place> resultList = placeDao.findByPrefNameTitleAndTypesAndNeedsReviewAndIdNot(
					place.getPrefName().getTitle(), "administrative-unit", false, place.getId());
			logger.debug("matched administrative units: " + resultList.size());
			if (resultList.size() == 1) {
				candidates.add(new Candidate(place, resultList.get(0), 1));
				return candidates;
			}
			
		} else if (place.getTypes().contains("populated-place") || place.getTypes() == null || place.getTypes().isEmpty()) {

			// XXX we suppose that the names of cities in the same country are unique
			Set<Place> resultList = new HashSet<Place>(placeDao.findByPrefNameTitleAndNeedsReviewAndIdNot(
					place.getPrefName().getTitle(), false, place.getId()));
			resultList.addAll(placeDao.findByNamesTitleAndNeedsReviewAndIdNot(
					place.getPrefName().getTitle(), false, place.getId()));
			for (PlaceName name : place.getNames()) {
				resultList.addAll(placeDao.findByPrefNameTitleAndNeedsReviewAndIdNot(
						name.getTitle(), false, place.getId()));
				resultList.addAll(placeDao.findByNamesTitleAndNeedsReviewAndIdNot(
						name.getTitle(), false, place.getId()));
			}
			logger.debug("matched populated places: " + resultList.size());
			
			if (place.getParent() == null) {
				if (resultList.size() == 1) {
					Place candidate = resultList.iterator().next();
					if (candidate.getParent() == null) {
						candidates.add(new Candidate(place, candidate, 1));
					}
				}
			} else {
				for (Place candidate : resultList) {
					// check ancestors for country with equal name 
					if (candidate.getParent() != null) {
						Place candidateAdministrativeUnit = retrieveAdministrativeUnitFor(candidate, false);
						Place placeAdministrativeUnit = retrieveAdministrativeUnitFor(place, false);
						logger.debug("comparing administrative units: {} == {}", placeAdministrativeUnit, candidateAdministrativeUnit);
						if (candidateAdministrativeUnit != null && placeAdministrativeUnit != null && 
								( idsMatch(placeAdministrativeUnit, candidateAdministrativeUnit) || namesMatch(placeAdministrativeUnit, candidateAdministrativeUnit) ) ) {
							logger.debug("administrative units matched");
							candidates.add(new Candidate(place, candidate, 1));
						}
					}
				}
			}
			
		}
		
		logger.debug("returning candidates: {}", candidates);
		
		return candidates;
		
	}
	
	private boolean idsMatch(Place place1, Place place2) {
		Set<Identifier> ids1 = new HashSet<Identifier>();
		for (Identifier id : place1.getIdentifiers()) ids1.add(id);
		logger.debug("place1 ids: {}", ids1);
		Set<Identifier> ids2 = new HashSet<Identifier>();
		for (Identifier id : place2.getIdentifiers()) ids2.add(id);
		logger.debug("place1 ids: {}", ids2);
		ids1.retainAll(ids2);
		logger.debug("matching ids {}", ids1);
		return !ids1.isEmpty();
	}
	
	private boolean namesMatch(Place place1, Place place2) {
		Set<String> names1 = new HashSet<String>();
		names1.add(place1.getPrefName().getTitle());
		for (PlaceName name : place1.getNames()) names1.add(name.getTitle());
		logger.debug("place1 names: {}", names1);
		Set<String> names2 = new HashSet<String>();
		names2.add(place2.getPrefName().getTitle());
		for (PlaceName name : place2.getNames()) names2.add(name.getTitle());
		logger.debug("place2 names: {}", names2);
		names1.retainAll(names2);
		logger.debug("matching names {}", names1);
		return !names1.isEmpty();
	}

	private Place retrieveAdministrativeUnitFor(Place place, boolean recursive) {
		if (place.getParent() == null) return null;
		Place parent = placeDao.findById(place.getParent()).orElse(null);
		if (parent == null) {
			return null;
		} else if (parent.getTypes().contains("administrative-unit")) {
			return parent;
		} else if (recursive) {
			return retrieveAdministrativeUnitFor(parent, true);
		} else {
			return null;
		}
	}

	@Override
	public void setPlaceRepository(PlaceRepository placeRepository) {
		placeDao = placeRepository;
	}

}
