package org.dainst.gazetteer.search;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.GeoBoundingBoxFilterBuilder;
import org.elasticsearch.index.query.GeoDistanceFilterBuilder;
import org.elasticsearch.index.query.GeoPolygonFilterBuilder;
import org.elasticsearch.index.query.GeoShapeFilterBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryFilterBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder.Operator;
import org.elasticsearch.index.query.functionscore.script.ScriptScoreFunctionBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.Facets;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchPlaceQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchPlaceQuery.class);
	
	private SearchRequestBuilder requestBuilder;
	private QueryBuilder queryBuilder;
	private long totalHits = -1;
	private Facets facets;

	public ElasticSearchPlaceQuery(Client client) {
		requestBuilder = client.prepareSearch("gazetteer").addField("_id");
	}
	
	public ElasticSearchPlaceQuery metaSearch(String query) {
		if(query == null || "".equals(query) || "*".equals(query)) listAll();
		// _id can't be added to _all, so it's appended here, titles are
		// added in order to boost them and prevent their score from being
		// diminished by norms when occurring together with other fields in _all
		else {
			String queryString = "(" + query + ")";
			queryString += " OR _id:\"" + query + "\"";
			if (!query.contains(":")) {
				queryString += " OR prefName.title:\"" + query + "\"^2";
				queryString += " OR names.title:\"" + query + "\"";
			}
			queryBuilder = QueryBuilders.queryString(queryString).defaultField("_all").defaultOperator(Operator.AND);
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
		queryBuilder = QueryBuilders.fuzzyLikeThisQuery(fields).likeText(query);
		return this;		
	}

	public ElasticSearchPlaceQuery prefixSearch(String query) {
		query = query.toLowerCase();
		queryBuilder = QueryBuilders.boolQuery()
				.should(QueryBuilders.termQuery("prefName.title.autocomplete", query))
				.should(QueryBuilders.termQuery("names.title.autocomplete", query));
		return this;
	}
	
	public ElasticSearchPlaceQuery geoDistanceSearch(double lon, double lat, int distance) {
		queryBuilder = QueryBuilders.matchAllQuery();
		GeoDistanceFilterBuilder filterBuilder = FilterBuilders.geoDistanceFilter("prefLocation.coordinates");
		filterBuilder.distance(Integer.toString(distance) + "km");
		filterBuilder.point(lat, lon);
		requestBuilder.setPostFilter(filterBuilder);
		return this;
	}
	
	public ElasticSearchPlaceQuery addBoostForChildren() {
		// places with many children should get a higher score
		
		queryBuilder = QueryBuilders.functionScoreQuery(queryBuilder)
				.add(new ScriptScoreFunctionBuilder().script("_score + (1.0 - 1.0 / ( 0.001 * doc['children'].value + 1.0 ) )").lang("groovy"));
		return this;
	}
	
	public ElasticSearchPlaceQuery addSort(String field, String order) {
		if ("asc".equals(order))
			requestBuilder.addSort(field, SortOrder.ASC);
		else
			requestBuilder.addSort(field, SortOrder.DESC);
		return this;
	}
	
	public ElasticSearchPlaceQuery addFacet(String field) {
		requestBuilder.addFacet(FacetBuilders.termsFacet(field).field(field).size(50));
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
	
	public ElasticSearchPlaceQuery addPolygonFilter(double[][] coordinates) {
		GeoPolygonFilterBuilder geoPolygonFilterBuilder = FilterBuilders.geoPolygonFilter("prefLocation.coordinates");
		PolygonBuilder polygonBuilder = ShapeBuilder.newPolygon();
		
		for (double[] lngLat : coordinates) {
			geoPolygonFilterBuilder.addPoint(lngLat[1], lngLat[0]);
			polygonBuilder.point(lngLat[0], lngLat[1]);
		}
		
		GeoShapeFilterBuilder geoShapeFilterBuilder = FilterBuilders.geoShapeFilter("prefLocation.shape", polygonBuilder, ShapeRelation.INTERSECTS);
		
		queryBuilder = QueryBuilders.filteredQuery(queryBuilder, FilterBuilders.orFilter(geoPolygonFilterBuilder, geoShapeFilterBuilder));
		return this;
	};

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
		logger.debug("Query: {}", queryBuilder.toString());
		SearchResponse response = requestBuilder.execute().actionGet();
		facets = response.getFacets();
		return responseAsList(response);	
	}
	
	public Facets getFacets() {
		return facets;
	}
	
	private String[] responseAsList(SearchResponse response) {
		SearchHits hits = response.getHits();
		totalHits = hits.getTotalHits();
		String[] result = new String[hits.hits().length];
		for (int i = 0; i < result.length; i++) {
			result[i] = hits.getAt(i).getId();
		}		
		return result;
	}

}
