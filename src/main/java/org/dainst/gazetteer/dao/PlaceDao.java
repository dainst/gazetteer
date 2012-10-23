package org.dainst.gazetteer.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Location;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceName;
import org.dainst.gazetteer.domain.Thesaurus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PlaceDao {
	
	@PersistenceContext
    private EntityManager em;
	
	public Place insert(Place place) {
		em.persist(place);
		em.refresh(place);
		return place;
	}
	
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
	
	public int deleteByThesaurus(Thesaurus thesaurus) {
		int i = 0;
		for (Place place : thesaurus.getPlaces()) {
			em.merge(place);
			for (Identifier id : place.getIdentifiers()) {
				em.merge(id);
				em.remove(id);
			}
			em.remove(place);
			i++;
		}		
		return i;
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
		return em.createQuery("SELECT p FROM Place p, IN(p.uris) u where u = :uri", Place.class)
				.setParameter("uri", uri).getSingleResult();		
	}

	public List<Place> list(int limit, int offset) {		
		return em.createQuery("SELECT p FROM Place p", Place.class)
				.setFirstResult(offset)
				.setMaxResults(limit)
				.getResultList();		
	}
	
	public List<Place> getPlacesByNameAndType(String name, String type) {
		return em.createQuery("SELECT p FROM Place p JOIN p.names n "
				+ "WHERE p.type = :type AND n.title = :name", Place.class)
				.setParameter("name", name)
				.setParameter("type", type)
				.getResultList();
	}
	
	public List<Place> getPlacesByNameAndTypeIncludingParent(
			String name, String type, String parentName, String parentType) {
		return em.createQuery("SELECT place FROM Place place JOIN p.names name "
				+ "JOIN place.parent parent JOIN parent.names parentName "
				+ "WHERE place.type = :type AND name.title = :name "
				+ "AND parent.type = :parent_type AND parentName.title = :parent_name", Place.class)
				.setParameter("name", name)
				.setParameter("type", type)
				.setParameter("parent_name", parentName)
				.setParameter("parent_type", parentType)
				.getResultList();
	}

}
