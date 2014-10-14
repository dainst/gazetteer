package org.dainst.gazetteer.search;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElasticSearchSuggestionQuery {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchSuggestionQuery.class);

	private SuggestRequestBuilder suggestRequestBuilder;

	public ElasticSearchSuggestionQuery(Client client) {
		suggestRequestBuilder = client.prepareSuggest("gazetteer");
	}
	
	public List<String> getSuggestions(String field, String text) {
		
		SuggestResponse response = suggestRequestBuilder.addSuggestion(new CompletionSuggestionBuilder("suggestions").field(field).text(text).size(10)).execute().actionGet();
		
	
		List<String> suggestions = new ArrayList<String>();
		
		if (response != null && response.getSuggest() != null && response.getSuggest().getSuggestion("suggestions") != null 
				&& response.getSuggest().getSuggestion("suggestions").getEntries() != null && response.getSuggest().getSuggestion("suggestions").getEntries().size() > 0) {
			
			Iterator<? extends Suggest.Suggestion.Entry.Option> iterator = response.getSuggest().getSuggestion("suggestions").getEntries().get(0).getOptions().iterator();
			
			LOGGER.debug(response.toString());
			
			while(iterator.hasNext()) {
				suggestions.add(iterator.next().getText().toString());
			}
		}
			
		return suggestions;
	}
}
