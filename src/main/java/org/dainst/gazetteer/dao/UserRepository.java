package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

	public User findByUsername(String username);
	public User findByEmail(String email);
}
