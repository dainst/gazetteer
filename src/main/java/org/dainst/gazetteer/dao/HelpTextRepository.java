package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.HelpText;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HelpTextRepository extends MongoRepository<HelpText, String> {
	
	public HelpText findByLanguageAndLoginNeeded(String language, boolean loginNeeded);
}
