package org.dainst.gazetteer.search;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ElasticSearchClientProvider {

	@Value("${cluster.name}")
	private String clusterName;
	
	private RestHighLevelClient client;
	
	public RestHighLevelClient getClient() {
		
		if (client == null) client = createClient();
		
		return client;
	}
	
	private RestHighLevelClient createClient() {
		
		return new RestHighLevelClient(
			RestClient.builder(new HttpHost("elasticsearch", 9200))
		);
	}
}
