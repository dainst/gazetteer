package org.dainst.gazetteer.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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
	
}
