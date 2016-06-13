package org.dainst.gazetteer.dao;

import org.dainst.gazetteer.domain.HelpText;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HelpTextRepository extends PagingAndSortingRepository<HelpText, String> {
	
	public HelpText findByLanguageAndLoginNeeded(String language, boolean loginNeeded);
}
