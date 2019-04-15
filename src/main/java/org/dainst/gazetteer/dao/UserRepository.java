package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

	public User findByUsername(String username);
	public User findByEmail(String email);
}
