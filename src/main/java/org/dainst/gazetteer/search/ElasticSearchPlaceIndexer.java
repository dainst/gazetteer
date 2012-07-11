package org.dainst.gazetteer.search;

import javax.ws.rs.core.MediaType;

import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;

public class ElasticSearchPlaceIndexer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPlaceIndexer.class);
	
	private ElasticSearchServer elasticSearchServer;
	
	private String baseUri;
	
	public ElasticSearchPlaceIndexer(ElasticSearchServer elasticSearchServer, String baseUri) {
		this.elasticSearchServer = elasticSearchServer;
		this.baseUri = baseUri;
	}
	
	public void deletePlaceFromIndex(long placeId) {		
		DeleteResponse deleteResponse = elasticSearchServer.getClient()
			.prepareDelete("gazetteer", "place", String.valueOf(placeId))
			.execute().actionGet();		
		LOGGER.info("deleted place from index: " + deleteResponse.getId());		
	}

	public void addPlaceToIndex(Place place) {
		LOGGER.info("starting indexing thread for place: " + place.getId());
		new Thread(new SinglePlaceIndexer(place, elasticSearchServer, baseUri)).start();
	}
	
	private static class SinglePlaceIndexer implements Runnable {
		
		private Place place;
		private ElasticSearchServer elasticSearchServer;
		private String baseUri;

		public SinglePlaceIndexer(Place place, ElasticSearchServer elasticSearchServer, String baseUri) {
			this.place = place;
			this.elasticSearchServer = elasticSearchServer;
			this.baseUri = baseUri;
		}

		@Override
		public void run() {
			
			String response = Client.create().resource(baseUri + "place/" + place.getId())
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(String.class);
			
			IndexResponse indexResponse = elasticSearchServer.getClient()
				.prepareIndex("gazetteer", "place", String.valueOf(place.getId()))
				.setSource(response).execute().actionGet();
			
			LOGGER.info("indexed place in document: " + indexResponse.getId());
			
		}		
		
	}
	
}
