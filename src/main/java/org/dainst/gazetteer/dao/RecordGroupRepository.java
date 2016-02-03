package org.dainst.gazetteer.dao;

import java.util.List;

import org.dainst.gazetteer.domain.RecordGroup;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RecordGroupRepository extends PagingAndSortingRepository<RecordGroup, String> {
	
	public RecordGroup findByName(String name);
	
	public List<RecordGroup> findByShowPlaces(boolean showPlaces);
}
