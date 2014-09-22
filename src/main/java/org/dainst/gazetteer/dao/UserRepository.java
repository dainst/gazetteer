package org.dainst.gazetteer.dao;

import java.util.List;

import org.dainst.gazetteer.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

	public User findById(String id);
	public User findByUsername(String username);
	public User findByEmail(String email);
	public List<User> findByRecordGroupIds(String recordGroupId);
}
