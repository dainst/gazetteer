package org.dainst.gazetteer.search;

import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.core.MediaType;

import org.apache.lucene.util.IOUtils;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.Requests;
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

	public void reindexAllPlaces() {
		LOGGER.info("reindexing all places");
		
		// 1. create new index
		
		// 2. create corresponding mongodb river
		
		// 3. change alias
		
		// 4. delete old index and river
		
		String esRiverJson = Thread.currentThread().getContextClassLoader().getResourceAsStream("es_river.json").toString();
		org.elasticsearch.client.Client client = elasticSearchServer.getClient();
		client.index(Requests.indexRequest("_river").type("mongodb").id("_meta").source(esRiverJson)).actionGet();
		
	}
	
	public void createIndexTemplate() {
		
		LOGGER.info("creating index template");
		
		InputStream esMappingStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("es_mapping.json");
		Scanner s = new java.util.Scanner(esMappingStream, "UTF-8");
		String esMappingJson = s.useDelimiter("\\A").next();
		s.close();
		LOGGER.debug("read mapping: {}", esMappingJson);
		
		InputStream esSettingsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("es_mapping.json");
		s = new java.util.Scanner(esSettingsStream, "UTF-8");
		String esSettingsJson = s.useDelimiter("\\A").next();
		s.close();
		LOGGER.debug("read mapping: {}", esMappingJson);
		
		IndicesAdminClient client = elasticSearchServer.getClient().admin().indices();
		new PutIndexTemplateRequestBuilder(client).setSettings(esSettingsJson)
			.addMapping("place", esMappingJson).execute();
		
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
