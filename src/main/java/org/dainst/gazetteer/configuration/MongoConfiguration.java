package org.dainst.gazetteer.configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.helpers.MongoBasedIncrementingIdGenerator;
import org.dainst.gazetteer.helpers.SimpleMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "org.dainst.gazetteer.dao")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        MongoConfiguration.class
    );

    private final MongoConfigurationProperties config;

    MongoConfiguration(final MongoConfigurationProperties config) {
        this.config = config;
    }

    @Override
    protected void configureClientSettings(
        MongoClientSettings.Builder builder
    ) {
        LOGGER.debug("Using MongoDB on {}", config.mongoReplicaSet());
        builder.applyConnectionString(
            new ConnectionString("mongodb://" + config.mongoReplicaSet())
        );
    }

    @Override
    protected String getDatabaseName() {
        return "gazetteer";
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoTemplate mongoTemplate = new MongoTemplate(
            mongoClient,
            "gazetteer"
        );
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return mongoTemplate;
    }

    @Bean
    public MongoBasedIncrementingIdGenerator idGenerator(
        MongoTemplate mongoTemplate
    ) {
        MongoBasedIncrementingIdGenerator mbiig =
            new MongoBasedIncrementingIdGenerator(
                mongoTemplate,
                "place_id_counter4",
                1000000
            );
        mbiig.setBlockSize(1000);
        return mbiig;
    }

    @Bean
    public SimpleMerger merger(PlaceRepository placeRepository) {
        SimpleMerger merger = new SimpleMerger();
        merger.setPlaceRepository(placeRepository);
        return merger;
    }

    @ConfigurationProperties
    static record MongoConfigurationProperties(String mongoReplicaSet) {}
}
