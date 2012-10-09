package org.dainst.gazetteer.search;

import org.elasticsearch.client.Client;

public interface ElasticSearchServer {

	public abstract Client getClient();
	
	public abstract void start();
	
	public abstract void stop();

}