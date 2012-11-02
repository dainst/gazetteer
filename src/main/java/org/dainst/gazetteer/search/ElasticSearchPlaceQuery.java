package org.dainst.gazetteer.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;

public class ElasticSearchPlaceQuery {
	
	//private static final Logger logger = LoggerFactory.getLogger(ElasticSearchPlaceQuery.class);
	
	private SearchRequestBuilder requestBuilder;
	private QueryBuilder queryBuilder;
	private long totalHits = -1;

	public ElasticSearchPlaceQuery(Client client) {
		requestBuilder = client.prepareSearch("gazetteer").addField("_id");
	}
	
	public ElasticSearchPlaceQuery metaSearch(String query) {
		if(query == null || "".equals(query) || "*".equals(query)) listAll();
		else queryBuilder = QueryBuilders.queryString(query + " OR _id:\"" + query + "\"")
				.defaultField("_all"); // _id can't be added to _all, so it's appended here
		return this;
	}
	
	public ElasticSearchPlaceQuery addBoostForChildren() {
		// places with many children should get a higher score
		queryBuilder = QueryBuilders.customScoreQuery(queryBuilder)
				.script("_score + (doc['children'].values.length / 1000)");
		return this;
	}
	
	public ElasticSearchPlaceQuery addSort(String field, String order) {
		if ("asc".equals(order))
			requestBuilder.addSort(field, SortOrder.ASC);
		else
			requestBuilder.addSort(field, SortOrder.DESC);
		return this;
	}

	public void listAll() {
		queryBuilder = QueryBuilders.matchAllQuery();		
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
		
		requestBuilder.setQuery(queryBuilder);
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
		queryBuilder = QueryBuilders.fuzzyQuery("_all", query);
		return this;
	}

	public ElasticSearchPlaceQuery fuzzyLikeThisSearch(String query, String... fields) {
		queryBuilder = QueryBuilders.fuzzyLikeThisQuery(fields).likeText(query).minSimilarity(0f);
		return this;		
	}

}
