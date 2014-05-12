package org.dainst.gazetteer.search;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchService.class);

	@Autowired
	private Client client;

	@Autowired
	private PlaceRepository placeDao;

	public void deletePlaceFromIndex(long placeId) {		
		DeleteResponse deleteResponse = client.prepareDelete("gazetteer", "place", String.valueOf(placeId))
				.execute().actionGet();		
		LOGGER.info("deleted place from index: " + deleteResponse.getId());		
	}

	public void reindexAllPlaces() {
		LOGGER.info("reindexing all places");
		
		// 1. create new index
		
		// 2. create corresponding mongodb river
		
		// 3. change alias
		
		// 4. delete old index and river
		
		String esRiverJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("es_river.json").toString();
		client.index(Requests.indexRequest("_river").type("mongodb").id("_meta").source(esRiverJson)).actionGet();
		
	}
}
