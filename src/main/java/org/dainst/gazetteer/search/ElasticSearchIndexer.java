package org.dainst.gazetteer.search;

import java.io.IOException;

import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.index.IndexRequest;
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
	
	public void index(Place place) {
		
		IndexRequest request = new IndexRequest("gazetteer", "place", place.getId());
		String jsonSource = getJsonSource(place);
		request.source(jsonSource, XContentType.JSON);
		
		logger.debug("Indexing place: " + jsonSource);
		
		try {
			clientProvider.getClient().index(request);
		} catch (IOException e) {
			logger.error("Failed to index place " + place.getId(), e);
		}
	}
	
	private String getJsonSource(Place place) {
		
		this.serializer.setIncludeAccessInfo(false);
		this.serializer.setIncludeChangeHistory(false);
		this.serializer.setPretty(false);
		
		return this.serializer.serializeIndexSource(place);
	}
	
}
