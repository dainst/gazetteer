package org.dainst.gazetteer.dao;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

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
	
}
