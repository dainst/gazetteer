package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.UserPasswordChangeRequest;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserPasswordChangeRequestRepository extends PagingAndSortingRepository<UserPasswordChangeRequest, String> {

	public UserPasswordChangeRequest findByUserId(String userId);
}
