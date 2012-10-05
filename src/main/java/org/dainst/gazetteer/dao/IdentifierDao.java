package org.dainst.gazetteer.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dainst.gazetteer.domain.Identifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class IdentifierDao {
	
	@PersistenceContext
    private EntityManager em;
	
	public Identifier getIdentifier(String value, String context) {
		TypedQuery<Identifier> query = em.createQuery(
				"SELECT FROM Identifier i WHERE i.value = :value AND i.context = :context",
				Identifier.class);
		return query.getSingleResult();
	}
	
}
