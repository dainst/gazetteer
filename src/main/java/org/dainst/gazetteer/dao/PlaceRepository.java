package org.dainst.gazetteer.dao;

import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.domain.Identifier;
import org.dainst.gazetteer.domain.Place;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PlaceRepository extends PagingAndSortingRepository<Place, String> {

	public Place getByUris(String uri);

	public List<Place> findByNamesTitleAndType(String name, String type);

	public List<Place> findByThesaurus(String thesaurus);

	public Place findByIds(Identifier id);

	public List<Place> findByIdIn(Set<String> children);

}
