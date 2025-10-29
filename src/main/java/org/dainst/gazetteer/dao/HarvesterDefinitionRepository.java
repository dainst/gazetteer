package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HarvesterDefinitionRepository extends CrudRepository<HarvesterDefinition, String> {

	public HarvesterDefinition getByName(String name);
	
}
