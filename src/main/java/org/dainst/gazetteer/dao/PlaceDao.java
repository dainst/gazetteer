package org.dainst.gazetteer.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PlaceDao {
	
	@PersistenceContext
    private EntityManager em;
	
	public Place save(Place place) {
        place = em.merge(place);
        return place;
    }

	public Place get(long placeId) {
		Place place = em.find(Place.class, placeId);
		return place;
	}

	public long delete(long placeId) {
		
		Place place = em.getReference(Place.class, placeId);
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
	
	public long setDeleted(long placeId) {
		
		Place place = em.getReference(Place.class, placeId);
		if (place != null) {
			place.setParent(null);
			for (Place child : place.getChildren()) {
				child.setParent(null);
			}
			// make a copy to prevent ConcurrentModificationException
			List<PlaceName> names = new ArrayList<PlaceName>(place.getNames());
			for (PlaceName name : names) {
				place.removeName(name);
				em.remove(name);
			}
			// make a copy to prevent ConcurrentModificationException
			Set<Location> locations = new HashSet<Location>(place.getLocations());
			for (Location location : locations) {
				place.removeLocation(location);
				em.remove(location);
			}
			place.setDeleted(true);
			em.merge(place);
			return placeId;
		} else {
			return 0;
		}
		
	}
	
	public long deleteName(long id) {
		PlaceName name = em.getReference(PlaceName.class, id);
		if (name != null) {
			em.remove(name);
			return id;
		} else {
			return 0;
		}
			
	}
	
	public long deleteLocation(long id) {
		Location location = em.getReference(Location.class, id);
		if (location != null) {
			em.remove(location);
			return id;
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
	
	public class Query {
				
		private TypedQuery<Place> query;

		public Query(String queryString) {
			query = em.createQuery(queryString, Place.class);
		}
		
		public Query setParameter(String key, String val) {
			query.setParameter(key, val);
			return this;
		}
		
		public List<Place> getResultList() {
			return query.getResultList();
		}
		
	}

}
