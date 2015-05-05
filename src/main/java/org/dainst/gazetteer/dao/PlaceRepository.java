package org.dainst.gazetteer.dao;

import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlaceRepository extends PagingAndSortingRepository<Place, String> {

	public Place getByLinksObjectAndLinksPredicate(String object, String predicate);
	
	@Query(value="{ '_id': ?0 }", fields="{ 'prefLocation.shape': 0 }")
	public Place findWithoutPolygon(String id);

	public List<Place> findByPrefNameTitle(String name);
	
	public List<Place> findByNamesTitle(String name);

	public List<Place> findByPrefNameTitleAndTypes(String name, String type);

	public Place findByIds(Identifier id);

	public Place findByIdsAndTypes(Identifier id, String type);
	
	public List<Place> findByParent(String parentId);
	
	public List<Place> findByParentAndDeletedIsFalse(String parentId);

	public List<Place> findByIdIn(Set<String> ids);

	public List<Place> findByParentIsNullAndDeletedIsFalse(Sort sort);

	public List<Place> findByTypesAndDeletedIsFalse(String string, Sort sort);

	public List<Place> findByPrefLocationIsNull();

	public List<Place> findByPrefLocationIsNullAndIdsContext(String context);

	public List<Place> findByPrefLocationIsNotNull();
	
	public List<Place> findByPrefLocationIsNotNull(Pageable pageable);
	
	public List<Place> findByRelatedPlaces(String id);

	public List<Place> findByNamesTitleAndTypes(String title, String string);

	public List<Place> findByNeedsReview(boolean b);

	public Place findByIdsAndTypesAndNeedsReviewAndIdNot(Identifier identifier,
			String type, boolean needsReview, String id);

	public List<Place> findByPrefNameTitleAndTypesAndNeedsReviewAndIdNot(
			String title, String string, boolean b, String id);

	public List<Place> findByPrefNameTitleAndNeedsReviewAndIdNot(String title, boolean b, String string);

	public List<Place> findByNamesTitleAndNeedsReviewAndIdNot(
			String title, boolean b, String id);

	public Place findByIdsAndNeedsReviewAndIdNot(Identifier id,
			boolean b, String id2);
	
	public List<Place> findByIdsContext(String context);

	public List<Place> findByProvenanceNotAndIdsContext(String string,
			String string2);
	
	public List<Place> findByRecordGroupIdAndDeletedIsFalse(String recordGroupId);
	
	@Query(value="{ 'children': { $gt: ?0 }, 'prefLocation.coordinates': { $exists: true } }", fields="{ 'prefLocation.shape': 0 }")
	public List<Place> findHeatmapPlaces(
			int i, PageRequest pageRequest);
}
