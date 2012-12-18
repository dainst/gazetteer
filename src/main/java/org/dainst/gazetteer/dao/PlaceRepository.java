package org.dainst.gazetteer.dao;

import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlaceRepository extends PagingAndSortingRepository<Place, String> {

	public Place getByLinksObjectAndLinksPredicate(String object, String predicate);

	public List<Place> findByPrefNameTitleAndType(String name, String type);

	public List<Place> findByThesauri(String thesaurus);

	public List<Place> findByThesauriAndParentIsNullAndDeletedIsFalse(String key, Sort sort);

	public List<Place> findByThesauriAndTypeAndDeletedIsFalse(String key, String type, Sort sort);

	public Place findByIds(Identifier id);

	public Place findByIdsAndType(Identifier id, String type);
	
	public List<Place> findByParent(String parentId);

	public List<Place> findByIdIn(Set<String> ids);

}
