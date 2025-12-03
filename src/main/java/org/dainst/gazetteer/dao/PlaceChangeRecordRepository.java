package org.dainst.gazetteer.dao;

import java.util.List;

import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface PlaceChangeRecordRepository extends MongoRepository<PlaceChangeRecord, String> {

	public List<PlaceChangeRecord> findByPlaceId(String placeId);
	public List<PlaceChangeRecord> findByUserId(String userId);
}
