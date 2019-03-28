package org.dainst.gazetteer.search;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;

public class ElasticSearchClientProvider {

	@Value("${cluster.name}")
	private String clusterName;
	
	@Value("${esNodes}")
	private String esNodes;
	
	private RestHighLevelClient client;
	
	public RestHighLevelClient getClient() {
		
		if (client == null) client = createClient();
		
		return client;
	}
	
	private RestHighLevelClient createClient() {
		
		RestClient lowLevelRestClient = RestClient
				.builder(new HttpHost("localhost", 9200, "http"))
				.build();
		
		return new RestHighLevelClient(lowLevelRestClient);
	}
}
