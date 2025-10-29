package org.dainst.gazetteer.configuration;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.helpers.MongoBasedIncrementingIdGenerator;
import org.dainst.gazetteer.helpers.SimpleMerger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.WriteResultChecking;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import com.mongodb.client.MongoClient;


@Configuration
@EnableMongoRepositories(basePackages = "org.dainst.gazetteer.dao")
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new ConnectionString("mongodb://mongodb"));
    }

    @Override
    protected String getDatabaseName() {
        return "gazetteer";
    }

    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient, "gazetteer");
        mongoTemplate.setWriteResultChecking(WriteResultChecking.EXCEPTION);
        return mongoTemplate;
    }

    @Bean
    public MongoBasedIncrementingIdGenerator idGenerator(MongoTemplate mongoTemplate) {
        MongoBasedIncrementingIdGenerator mbiig = new MongoBasedIncrementingIdGenerator(mongoTemplate, "place_id_counter4", 1000000);
        mbiig.setBlockSize(1000);
        return mbiig;
    }

    @Bean
    public SimpleMerger merger(PlaceRepository placeRepository) {
        SimpleMerger merger = new SimpleMerger();
        merger.setPlaceRepository(placeRepository);
        return merger;
    }
}
