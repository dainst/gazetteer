package org.dainst.gazetteer.search;

import java.util.Map;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteElasticSearchServer implements ElasticSearchServer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteElasticSearchServer.class);
	private Map<String,Integer> hosts;
	private Map<String,String> configuration;
	private Client client;

	public RemoteElasticSearchServer(Map<String,Integer> hosts, Map<String,String> configuration) {
		this.hosts = hosts;
		this.configuration = configuration;
	}

	public void start() {
		
		LOGGER.info("Starting elasticsearch transport client.");

		Settings settings = ImmutableSettings.settingsBuilder()
				.put(configuration).build();
		TransportClient transportClient = new TransportClient(settings);
		for (String host : hosts.keySet()) {
			LOGGER.debug("Adding {}:{} as elastic search node.", host, hosts.get(host));
			transportClient.addTransportAddress(new InetSocketTransportAddress(host, hosts.get(host)));
		}
		client = transportClient;
		
	}

	public void stop() {
		if (client == null) throw new IllegalStateException("Client not started. start() has to be called first.");
		client.close();
	}

	@Override
	public Client getClient() {
		if (client == null) throw new IllegalStateException("Client not started. start() has to be called first.");
		return client;
	}

}
