package org.dainst.gazetteer.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.Thesaurus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ThesaurusDao {
	
	@PersistenceContext
    private EntityManager em;
	
	public Thesaurus save(Thesaurus thesaurus) {
        thesaurus = em.merge(thesaurus);
        return thesaurus;
    }

	public Thesaurus getThesaurusByKey(String key) {
		Query query = em.createQuery("SELECT t FROM Thesaurus t where t.key = :key");
		query.setParameter("key", key);
		try {
			return (Thesaurus) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	public void delete(Thesaurus thesaurus) {
		TypedQuery<Place> query = em.createQuery("SELECT p FROM Place p where p.thesaurus = :thesaurus", Place.class);
		query.setParameter("thesaurus", thesaurus);
		List<Place> places = query.getResultList();
		for (Place place : places) {
			em.remove(place);
		}
		em.remove(thesaurus);
	}
	
}
