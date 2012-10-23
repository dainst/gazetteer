package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.Thesaurus;
import org.springframework.data.repository.CrudRepository;

public interface ThesaurusRepository extends CrudRepository<Thesaurus, String> {

	public Thesaurus getThesaurusByKey(String key);
	
}
