package org.dainst.gazetteer.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dainst.gazetteer.domain.Place;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PlaceDao {
	
	@PersistenceContext
    private EntityManager em;
	
	public Place save(Place place) {
        em.persist(place);
        return place;
    }

	public Place get(long placeId) {
		Place place = em.find(Place.class, placeId);
		return place;
	}

	public boolean delete(long placeId) {
		
		Place place = em.find(Place.class, placeId);
		if (place != null) {
			place.setParent(null);
			for (Place child : place.getChildren()) {
				child.setParent(null);
			}
			em.remove(place);
			return true;
		} else {
			return false;
		}
	}

}
