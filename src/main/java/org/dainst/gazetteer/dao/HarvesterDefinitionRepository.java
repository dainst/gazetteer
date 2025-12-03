package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.HarvesterDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HarvesterDefinitionRepository extends MongoRepository<HarvesterDefinition, String> {

	public HarvesterDefinition getByName(String name);
	
}
