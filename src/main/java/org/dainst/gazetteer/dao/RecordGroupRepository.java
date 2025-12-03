package org.dainst.gazetteer.dao;

import java.util.List;

import org.dainst.gazetteer.domain.RecordGroup;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface RecordGroupRepository extends MongoRepository<RecordGroup, String> {
	
	public RecordGroup findByName(String name);
	
	public List<RecordGroup> findByShowPlaces(boolean showPlaces);
}
