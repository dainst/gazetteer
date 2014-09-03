package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.UserGroup;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserGroupRepository extends PagingAndSortingRepository<UserGroup, String> {
	
	public UserGroup findByName(String name);
}
