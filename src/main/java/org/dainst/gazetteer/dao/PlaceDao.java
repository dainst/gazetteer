package org.dainst.gazetteer.dao;

import java.util.List;

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

	public long delete(long placeId) {
		
		Place place = em.find(Place.class, placeId);
		if (place != null) {
			place.setParent(null);
			for (Place child : place.getChildren()) {
				child.setParent(null);
			}
			em.remove(place);
			return placeId;
		} else {
			return 0;
		}
		
	}

	public Place getPlaceByUri(String uri) {		
		return (Place) em.createQuery("SELECT p FROM Place p, IN(p.uris) u where u = :uri")
				.setParameter("uri", uri).getSingleResult();		
	}

	public List<Place> list(int limit, int offset) {		
		return em.createQuery("SELECT p FROM Place p", Place.class)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();		
	}

}
