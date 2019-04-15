package org.dainst.gazetteer.search;

import java.io.IOException;
import java.util.List;

import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class ElasticSearchIndexer {
	
	private final static Logger logger = LoggerFactory.getLogger("org.dainst.gazetteer.ElasticSearchIndexer");
	
	@Autowired
	private ElasticSearchClientProvider clientProvider;
	
	@Autowired
	private JsonPlaceSerializer serializer;
	
	public void index(List<Place> places) {
		
		BulkRequest request = new BulkRequest();
		for (Place place : places) {
			request.add(createIndexRequest(place));
		}
		
		try {
			clientProvider.getClient().bulk(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.error("Failed to index places", e);
		}
	}
	
	public void index(Place place) {
		
		IndexRequest request = createIndexRequest(place);
		
		try {
			clientProvider.getClient().index(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			logger.error("Failed to index place " + place.getId(), e);
		}
	}
	
	private IndexRequest createIndexRequest(Place place) {
		
		IndexRequest request = new IndexRequest("places");
		request.id(place.getId());
		request.source(getJsonSource(place), XContentType.JSON);
		
		return request;
	}
	
	private String getJsonSource(Place place) {
		
		this.serializer.setIncludeAccessInfo(false);
		this.serializer.setIncludeChangeHistory(false);
		this.serializer.setPretty(false);
		
		return this.serializer.serializeIndexSource(place);
	}
	
}
