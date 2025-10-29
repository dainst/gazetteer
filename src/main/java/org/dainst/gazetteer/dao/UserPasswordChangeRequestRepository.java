package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.UserPasswordChangeRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserPasswordChangeRequestRepository extends MongoRepository<UserPasswordChangeRequest, String> {

	public UserPasswordChangeRequest findByUserId(String userId);
}
