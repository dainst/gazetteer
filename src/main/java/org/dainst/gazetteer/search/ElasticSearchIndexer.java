package org.dainst.gazetteer.search;

import java.io.IOException;
import java.util.List;
import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.domain.Place;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ElasticSearchIndexer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ElasticSearchIndexer.class
    );

    private final RestHighLevelClient client;
    private final JsonPlaceSerializer serializer;

    ElasticSearchIndexer(
        final RestHighLevelClient client,
        final JsonPlaceSerializer serializer
    ) {
        this.client = client;
        this.serializer = serializer;
    }

    public void index(List<Place> places) {
        BulkRequest request = new BulkRequest();
        for (Place place : places) {
            request.add(createIndexRequest(place));
        }

        try {
            client.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.error("Failed to index places", e);
        }
    }

    public void index(Place place) {
        IndexRequest request = createIndexRequest(place);

        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            LOGGER.error("Failed to index place " + place.getId(), e);
        }
    }

    private IndexRequest createIndexRequest(Place place) {
        IndexRequest request = new IndexRequest(
            "gazetteer",
            "place",
            place.getId()
        );
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
