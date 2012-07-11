package org.dainst.gazetteer.search;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;

import org.aspectj.lang.ProceedingJoinPoint;
import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.index.IndexResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;

import com.sun.jersey.api.client.Client;

public class ElasticSearchPlaceIndexer implements Ordered {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchPlaceIndexer.class);
	
	@Resource(name="elasticSearchServer")
	private ElasticSearchServer elasticSearchServer;
	
	@Value("${baseUri}")
	private String baseUri;
	
	private int order = 1;
	
	public ElasticSearchPlaceIndexer() {
		LOGGER.info("constructing");
	}

	public Place indexPlace(ProceedingJoinPoint pjp) {
		
		LOGGER.info("indexing place before");
		
		Place place;
		try {
			place = (Place) pjp.proceed();
		} catch (Throwable e) {
			LOGGER.error("Error while trying to index saved place.", e);
			return null;
		}
		
		LOGGER.info("starting indexing thread for place: " + place.getId());
		
		new Thread(new SinglePlaceIndexer(place, elasticSearchServer, baseUri)).start();
		
		return place;
	}

	public void setElasticSearchServer(ElasticSearchServer elasticSearchServer) {
		LOGGER.info("setElasticSearchServer called");
		this.elasticSearchServer = elasticSearchServer;
	}

	@Override
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
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
			
			if (elasticSearchServer == null) {
				LOGGER.error("elasticSearchServer has not been set. Unable to index place");
				return;
			}
			
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
