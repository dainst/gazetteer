package org.dainst.gazetteer.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class HarvesterDefinitionDao {
	
	@PersistenceContext
    private EntityManager em;

	public HarvesterDefinition save(HarvesterDefinition defintion) {
		defintion = em.merge(defintion);
		return defintion;
	}
	
	public List<HarvesterDefinition> list() {
		return em.createQuery("SELECT h from HarvesterDefinition h",
				HarvesterDefinition.class).getResultList();
	}
	
}
