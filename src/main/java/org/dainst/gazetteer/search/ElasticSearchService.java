package org.dainst.gazetteer.search;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.indices.IndexAlreadyExistsException;
import org.elasticsearch.indices.TypeMissingException;
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
		
		try {			
			// recreate es index
			client.admin().indices().delete(new DeleteIndexRequest("gazetteer")).get();
			client.admin().indices().create(new CreateIndexRequest("gazetteer")).get();			
		} catch (Exception e) {
			throw new RuntimeException("Failed to reindex places", e);
		}
			
		// recreate corresponding mongodb river
		try {
			client.admin().indices().create(new CreateIndexRequest("_river")).get();
			client.admin().indices().deleteMapping(new DeleteMappingRequest("_river").types("mongodb")).get();
		} catch (InterruptedException e) {
			throw new RuntimeException("Failed to reindex places", e);
		} catch (ExecutionException e) {
			// eat exceptions if river is created for the first time
		}
		
		try {
			InputStream esRiverStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("es/mongodb_river.json");
			Scanner s = new Scanner(esRiverStream);
			String esRiverJson = s.useDelimiter("\\A").next();
			s.close();
			esRiverStream.close();
			LOGGER.debug("esRiverJson: {}", esRiverJson);
			IndexResponse indexResponse = client.index(Requests.indexRequest("_river").type("mongodb").id("_meta").source(esRiverJson)).get();
			LOGGER.debug("created: {}", indexResponse.isCreated());
		} catch (Exception e) {
			throw new RuntimeException("Failed to reindex places", e);
		}
		
	}
}
