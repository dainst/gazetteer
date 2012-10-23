package org.dainst.gazetteer.search;

import java.math.BigInteger;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchPlaceQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchPlaceQuery.class);
	
	private SearchRequestBuilder requestBuilder;
	private long totalHits = -1;

	public ElasticSearchPlaceQuery(Client client) {
		requestBuilder = client.prepareSearch("gazetteer").addField("_id");
	}
	
	public ElasticSearchPlaceQuery metaSearch(String query) {
		if("".equals(query) || "*".equals(query)) requestBuilder.setQuery(QueryBuilders.matchAllQuery());
		else requestBuilder.setQuery(QueryBuilders.textQuery("_all", query));
		return this;
	}

	public void listAll() {
		requestBuilder.setQuery(QueryBuilders.matchAllQuery());		
	}
	
	public ElasticSearchPlaceQuery offset(int offset) {
		requestBuilder.setFrom(offset);
		return this;
	}
	
	public ElasticSearchPlaceQuery limit(int limit) {
		requestBuilder.setSize(limit);
		return this;
	}
	
	public long getHits() {
		return totalHits;
	}
	
	public String[] execute() {
		
		SearchResponse response = requestBuilder.execute().actionGet();
		SearchHits hits = response.hits();
		totalHits = hits.getTotalHits();
		String[] result = new String[hits.hits().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = hits.getAt(i).getId();
		}
		
		return result;
		
	}

	public ElasticSearchPlaceQuery fuzzySearch(String query) {
		requestBuilder.setQuery(QueryBuilders.fuzzyQuery("_all", query));
		return this;
	}

	public ElasticSearchPlaceQuery fuzzyLikeThisSearch(String query, String... fields) {
		requestBuilder.setQuery(QueryBuilders.fuzzyLikeThisQuery(fields).likeText(query).minSimilarity(0f));
		return this;		
	}

}
