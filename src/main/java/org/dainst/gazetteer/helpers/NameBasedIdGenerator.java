package org.dainst.gazetteer.helpers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.Normalizer;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NameBasedIdGenerator implements IdGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(NameBasedIdGenerator.class);
	
	private int length;
	
	private SecureRandom random = new SecureRandom();
	
	private PlaceRepository placeRepository;

	public String generate(Place place) {
		
		String id;
		if (place.getNames().size() > 0) {
			id = place.getNames().get(0).getTitle();
			if (id.length() > length) id = id.substring(0, length);
			id = id.toLowerCase();
			id = Normalizer.normalize(id, Normalizer.Form.NFD)
	           .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^\\p{Alnum}]", "");
		} else {
			id = new BigInteger(length*5, random).toString(32).toLowerCase();
		}
		
		// check DB if id is unique
		if (getPlaceRepository() == null) {
			logger.warn("no place repository set, uniqueness cannot be verified");
			return id;
		}
		int i = 0;
		String baseId = id;
		while (getPlaceRepository().findOne(id) != null) {
			id = baseId + String.valueOf(++i);
		}
		
		return id;
		
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public PlaceRepository getPlaceRepository() {
		return placeRepository;
	}

	public void setPlaceRepository(PlaceRepository placeRep) {
		this.placeRepository = placeRep;
	}

}
