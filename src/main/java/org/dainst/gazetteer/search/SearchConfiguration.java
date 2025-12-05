package org.dainst.gazetteer.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.RestHighLevelClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SearchConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        SearchConfiguration.class
    );

    @Bean
    public RestHighLevelClient elasticsearchClient(
        ClusterConfigurationProperties config
    ) {
        LOGGER.debug(
            "Using Elasticsearch on {}:{}",
            config.getName(),
            config.getPort()
        );
        final var restClient = RestClient.builder(
            new HttpHost(config.getName(), config.getPort())
        ).build();
        return new RestHighLevelClientBuilder(restClient)
            .setApiCompatibilityMode(config.isCompatiblityMode())
            .build();
    }

    @ConfigurationProperties(prefix = "cluster")
    static class ClusterConfigurationProperties {

        @NotBlank
        private String name;

        @Min(1)
        @Max(65535)
        private int port = 9200;

        private boolean compatiblityMode = true;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public boolean isCompatiblityMode() {
            return compatiblityMode;
        }

        public void setCompatiblityMode(boolean compatiblityMode) {
            this.compatiblityMode = compatiblityMode;
        }
    }
}
