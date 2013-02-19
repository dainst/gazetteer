package org.dainst.gazetteer.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoBoundingBoxFilterBuilder;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryFilterBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
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
		// _id can't be added to _all, so it's appended here, prefName.title is
		// added in order to boost it and prevent its score from being
		// diminished by norms when occurring together with other fields in _all
		else {
			String queryString = query + " OR _id:\"" + query + "\"";
			if (!query.contains(":")) queryString += " OR prefName.title:" + query;
			queryBuilder = QueryBuilders.queryString(queryString).defaultField("_all");
		}
				
		return this;
		
	}
	
	public ElasticSearchPlaceQuery extendedSearch(String jsonQuery) {
		queryBuilder = QueryBuilders.wrapperQuery(jsonQuery);
		return this;
	}
	
	public ElasticSearchPlaceQuery queryStringSearch(String query) {
		queryBuilder = QueryBuilders.queryString(query).defaultField("_all");
		return this;
	}

	public ElasticSearchPlaceQuery fuzzySearch(String query) {
		queryBuilder = QueryBuilders.fuzzyQuery("_all", query);
		return this;
	}

	public ElasticSearchPlaceQuery fuzzyLikeThisSearch(String query, String... fields) {
		queryBuilder = QueryBuilders.fuzzyLikeThisQuery(fields).likeText(query).minSimilarity(0f);
		return this;		
	}

	public ElasticSearchPlaceQuery prefixSearch(String query) {
		query = query.toLowerCase();
		queryBuilder = QueryBuilders.prefixQuery("_all", query);
		return this;
	}
	
	public ElasticSearchPlaceQuery geoDistanceSearch(double lon, double lat, int distance) {
		queryBuilder = QueryBuilders.matchAllQuery();
		GeoDistanceFilterBuilder filterBuilder = FilterBuilders.geoDistanceFilter("prefLocation.coordinates");
		filterBuilder.distance(Integer.toString(distance) + "km");
		filterBuilder.point(lat, lon);
		requestBuilder.setFilter(filterBuilder);
		return this;
	}
	
	public ElasticSearchPlaceQuery addBoostForChildren() {
		// places with many children should get a higher score
		queryBuilder = QueryBuilders.customScoreQuery(queryBuilder)
				.script("_score + (doc['children'].value / 100)");
		return this;
	}
	
	public ElasticSearchPlaceQuery addSort(String field, String order) {
		if ("asc".equals(order))
			requestBuilder.addSort(field, SortOrder.ASC);
		else
			requestBuilder.addSort(field, SortOrder.DESC);
		return this;
	}
	
	public ElasticSearchPlaceQuery addGeoDistanceSort(double lon, double lat) {
		GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort("prefLocation.coordinates");
		sortBuilder.order(SortOrder.ASC);
		sortBuilder.point(lat, lon);
		requestBuilder.addSort(sortBuilder);
		return this;
	}
	
	public ElasticSearchPlaceQuery addFilter(String filterQuery) {
		QueryFilterBuilder filterBuilder = FilterBuilders.queryFilter(QueryBuilders.queryString(filterQuery));
		queryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);
		return this;
	}
	
	public ElasticSearchPlaceQuery addBBoxFilter(
			double northLat, double eastLon, 
			double southLat, double westLon) {
		GeoBoundingBoxFilterBuilder filterBuilder = FilterBuilders.geoBoundingBoxFilter("prefLocation.coordinates")
				.topLeft(northLat, westLon).bottomRight(southLat, eastLon);
		queryBuilder = QueryBuilders.filteredQuery(queryBuilder, filterBuilder);
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
		return responseAsList(response);	
	}
	
	private String[] responseAsList(SearchResponse response) {
		SearchHits hits = response.hits();
		totalHits = hits.getTotalHits();
		String[] result = new String[hits.hits().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = hits.getAt(i).getId();
		}		
		return result;
	}

}
