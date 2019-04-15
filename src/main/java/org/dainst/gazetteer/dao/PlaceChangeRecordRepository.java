package org.dainst.gazetteer.dao;

import java.util.List;

import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlaceChangeRecordRepository extends PagingAndSortingRepository<PlaceChangeRecord, String> {

	public List<PlaceChangeRecord> findByPlaceId(String placeId);
	public List<PlaceChangeRecord> findByUserId(String userId);
}
