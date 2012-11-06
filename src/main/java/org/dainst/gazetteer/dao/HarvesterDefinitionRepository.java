package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.springframework.data.repository.CrudRepository;

public interface HarvesterDefinitionRepository extends CrudRepository<HarvesterDefinition, String> {

	public HarvesterDefinition getByName(String name);
	
}
