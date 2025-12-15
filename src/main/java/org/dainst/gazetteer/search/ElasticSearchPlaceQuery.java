package org.dainst.gazetteer.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.ShapeRelation;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder;
import org.elasticsearch.index.query.functionscore.ScriptScoreFunctionBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.locationtech.jts.geom.Coordinate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchPlaceQuery {

    private static final Logger logger = LoggerFactory.getLogger(
        ElasticSearchPlaceQuery.class
    );

    private final RestHighLevelClient client;
    private final SearchSourceBuilder searchSourceBuilder;
    private final BoolQueryBuilder queryBuilder;
    private long totalHits = -1;
    private Aggregations aggregations;
    private boolean childrenBoost = false;
    private String lastScrollId;

    // TODO: Better have a factory than pass around the client reference
    public ElasticSearchPlaceQuery(final RestHighLevelClient client) {
        this.client = client;
        this.searchSourceBuilder = new SearchSourceBuilder();
        this.queryBuilder = QueryBuilders.boolQuery();
    }

    public ElasticSearchPlaceQuery metaSearch(final String query) {
        if (query == null || "".equals(query) || "*".equals(query)) listAll();
        // _id can't be added to all, so it's appended here, titles are
        // added in order to boost them and prevent their score from being
        // diminished by norms when occurring together with other fields in all
        else {
            String queryString = "(" + query + ")";
            queryString += " OR _id:\"" + query + "\"";
            if (!query.contains(":")) {
                queryString += " OR prefName.title:\"" + query + "\"^2";
                queryString += " OR names.title:\"" + query + "\"";
            }
            queryBuilder.must(
                QueryBuilders.queryStringQuery(queryString)
                    .defaultField("all")
                    .defaultOperator(Operator.AND)
            );
        }

        return this;
    }

    public ElasticSearchPlaceQuery extendedSearch(final String jsonQuery) {
        queryBuilder.must(QueryBuilders.wrapperQuery(jsonQuery));
        return this;
    }

    public ElasticSearchPlaceQuery queryStringSearch(final String query) {
        queryBuilder.must(
            QueryBuilders.queryStringQuery(query).defaultField("all")
        );
        return this;
    }

    public ElasticSearchPlaceQuery fuzzySearch(final String query) {
        queryBuilder.must(QueryBuilders.fuzzyQuery("all", query));
        return this;
    }

    public ElasticSearchPlaceQuery prefixSearch(String query) {
        query = query.toLowerCase();
        queryBuilder.must(
            QueryBuilders.boolQuery()
                .should(
                    QueryBuilders.termQuery(
                        "prefName.title.autocomplete",
                        query
                    )
                )
                .should(
                    QueryBuilders.termQuery("names.title.autocomplete", query)
                )
        );
        return this;
    }

    public ElasticSearchPlaceQuery geoDistanceSearch(
        final double lon,
        final double lat,
        final int distance
    ) {
        final GeoDistanceQueryBuilder geoDistanceQueryBuilder =
            QueryBuilders.geoDistanceQuery("prefLocation.coordinates");
        geoDistanceQueryBuilder.distance(Integer.toString(distance) + "km");
        geoDistanceQueryBuilder.point(lat, lon);
        queryBuilder.must(geoDistanceQueryBuilder);
        return this;
    }

    public void addBoostForChildren() {
        childrenBoost = true;
    }

    public ElasticSearchPlaceQuery addSort(
        final String field,
        final String order
    ) {
        if ("asc".equals(order)) searchSourceBuilder.sort(field, SortOrder.ASC);
        else searchSourceBuilder.sort(field, SortOrder.DESC);
        return this;
    }

    public ElasticSearchPlaceQuery addTermsAggregation(final String field) {
        final TermsAggregationBuilder aggregation = AggregationBuilders.terms(
            field
        )
            .field(field)
            .size(50);
        searchSourceBuilder.aggregation(aggregation);
        return this;
    }

    public ElasticSearchPlaceQuery addGeoDistanceSort(
        final double lon,
        final double lat
    ) {
        final GeoDistanceSortBuilder sortBuilder = SortBuilders.geoDistanceSort(
            "prefLocation.coordinates",
            new GeoPoint(lat, lon)
        );
        sortBuilder.order(SortOrder.ASC);
        searchSourceBuilder.sort(sortBuilder);
        return this;
    }

    public ElasticSearchPlaceQuery addFilter(final String filterQuery) {
        queryBuilder.filter(QueryBuilders.queryStringQuery(filterQuery));
        return this;
    }

    public ElasticSearchPlaceQuery addBBoxFilter(
        final double northLat,
        final double eastLon,
        final double southLat,
        final double westLon
    ) {
        final GeoBoundingBoxQueryBuilder boundingBoxQueryBuilder =
            QueryBuilders.geoBoundingBoxQuery(
                "prefLocation.coordinates"
            ).setCorners(northLat, westLon, southLat, eastLon);
        queryBuilder.filter(boundingBoxQueryBuilder);
        return this;
    }

    public ElasticSearchPlaceQuery addPolygonFilter(
        final double[][] coordinates
    ) {
        final List<GeoPoint> points = new ArrayList<>();
        final List<Coordinate> coords = new ArrayList<>();

        for (final double[] lngLat : coordinates) {
            points.add(new GeoPoint(lngLat[1], lngLat[0]));
            coords.add(new Coordinate(lngLat[0], lngLat[1]));
        }

        try {
            final var geoPolygonQueryBuilder = QueryBuilders.geoPolygonQuery(
                "prefLocation.coordinates",
                points
            );
            final var geoShapeQueryBuilder = QueryBuilders.geoShapeQuery(
                "prefLocation.shape",
                Queries.polygonFromCoordinates(coordinates)
            );
            geoShapeQueryBuilder.relation(ShapeRelation.INTERSECTS);

            final BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder
                .should(geoPolygonQueryBuilder)
                .should(geoShapeQueryBuilder);

            queryBuilder.filter(boolQueryBuilder);
        } catch (final IOException e) {
            logger.error(
                "Failed to create polygon filter query for coordinates: " +
                    coordinates,
                e
            );
        }

        return this;
    }

    public void listAll() {
        queryBuilder.must(QueryBuilders.matchAllQuery());
    }

    public ElasticSearchPlaceQuery offset(final int offset) {
        searchSourceBuilder.from(offset);
        return this;
    }

    public ElasticSearchPlaceQuery limit(final int limit) {
        searchSourceBuilder.size(limit);
        return this;
    }

    public long getHits() {
        return totalHits;
    }

    public String[] execute() {
        return execute(false);
    }

    public String[] execute(final boolean startScroll) {
        final SearchRequest request = getSearchRequest();
        if (startScroll) request.scroll(TimeValue.timeValueMinutes(30L));

        logger.debug("Query: {}", queryBuilder.toString());
        try {
            final SearchResponse response = client.search(
                request,
                RequestOptions.DEFAULT
            );
            aggregations = response.getAggregations();
            if (startScroll) lastScrollId = response.getScrollId();
            return responseAsList(response);
        } catch (final IOException e) {
            logger.error("Error while executing search query", e);
            return new String[0];
        }
    }

    public String[] execute(final String scrollId) {
        final SearchScrollRequest request = new SearchScrollRequest(scrollId);
        request.scroll(TimeValue.timeValueMinutes(30L));

        try {
            final SearchResponse response = client.scroll(
                request,
                RequestOptions.DEFAULT
            );
            aggregations = response.getAggregations();
            lastScrollId = response.getScrollId();
            return responseAsList(response);
        } catch (final IOException e) {
            logger.error("Error while executing search query", e);
            return new String[0];
        }
    }

    public String getScrollId() {
        return lastScrollId;
    }

    public Aggregations getTermsAggregations() {
        return aggregations;
    }

    private SearchRequest getSearchRequest() {
        if (childrenBoost) searchSourceBuilder.query(
            addChildrenBoostScriptFunction(queryBuilder)
        );
        else searchSourceBuilder.query(queryBuilder);

        final SearchRequest request = new SearchRequest("gazetteer");
        request.source(searchSourceBuilder);
        request.types("place");

        return request;
    }

    private String[] responseAsList(final SearchResponse response) {
        final SearchHits hits = response.getHits();
        totalHits = hits.getTotalHits().value;
        final String[] result = new String[hits.getHits().length];
        for (int i = 0; i < result.length; i++) {
            result[i] = hits.getAt(i).getId();
        }
        return result;
    }

    private FunctionScoreQueryBuilder addChildrenBoostScriptFunction(
        final BoolQueryBuilder query
    ) {
        // places with many children should get a higher score
        final Script script = new Script(
            ScriptType.INLINE,
            "painless",
            "_score + (1.0 - 1.0 / ( 0.001 * doc['children'].value + 1.0 ) )",
            new HashMap<String, Object>()
        );

        final FilterFunctionBuilder[] functions = {
            new FunctionScoreQueryBuilder.FilterFunctionBuilder(
                new ScriptScoreFunctionBuilder(script)
            ),
        };

        return QueryBuilders.functionScoreQuery(query, functions);
    }
}
