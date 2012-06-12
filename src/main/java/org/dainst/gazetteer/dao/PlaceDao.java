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
	
	public void save(Place place) {
        em.persist(place);
    }

}
