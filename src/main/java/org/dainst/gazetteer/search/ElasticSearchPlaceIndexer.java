package org.dainst.gazetteer.search;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.sun.jersey.api.client.Client;

public class ElasticSearchPlaceIndexer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPlaceIndexer.class);

	private ElasticSearchServer elasticSearchServer;

	private String baseUri;

	@Autowired
	private PlaceRepository placeDao;

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

	// done via reinstantiating mongodb river
	@Deprecated
	public void reindexAllPlaces() {
		LOGGER.info("reindexing all places");
		new Thread(new AllPlaceIndexer(placeDao, elasticSearchServer, baseUri)).start();
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

	private static class AllPlaceIndexer implements Runnable {

		private PlaceRepository placeDao;
		private ElasticSearchServer elasticSearchServer;
		private String baseUri;

		public AllPlaceIndexer(PlaceRepository placeDao, ElasticSearchServer elasticSearchServer, String baseUri) {
			this.placeDao = placeDao;
			this.elasticSearchServer = elasticSearchServer;
			this.baseUri = baseUri;
		}

		@Override
		public void run() {

			Date start = new Date();

			elasticSearchServer.getClient().admin().indices()
			.delete(new DeleteIndexRequest("gazetteer"))
			.actionGet();

			Iterable<Place> places = placeDao.findAll();
			for (Place place : places) {

				if (place.isDeleted()) continue;

				String response = Client.create().resource(baseUri + "place/" + place.getId())
						.accept(MediaType.APPLICATION_JSON_TYPE)
						.get(String.class);

				IndexResponse indexResponse = elasticSearchServer.getClient()
						.prepareIndex("gazetteer", "place", String.valueOf(place.getId()))
						.setSource(response).execute().actionGet();

				LOGGER.trace("indexed place in document: " + indexResponse.getId());

			}

			elasticSearchServer.getClient().admin().indices()
			.flush(new FlushRequest("gazetteer").refresh(true))
			.actionGet();

			Date end = new Date();
			long duration = end.getTime() - start.getTime();

			LOGGER.info("OK: successfully performed complete reindexing in {}ms", duration);

		}

	}

}
