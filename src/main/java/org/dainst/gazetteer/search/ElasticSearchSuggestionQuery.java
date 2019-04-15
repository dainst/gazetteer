package org.dainst.gazetteer.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.elasticsearch.search.suggest.completion.context.CategoryQueryContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

public class ElasticSearchSuggestionQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchSuggestionQuery.class);
	
	private RestHighLevelClient client;
	private RecordGroupRepository recordGroupDao;
	private GroupRoleRepository groupRoleDao;

	private static int size = 7;

	public ElasticSearchSuggestionQuery(RestHighLevelClient client, RecordGroupRepository groupDao, GroupRoleRepository groupRoleDao) {

		this.client = client;
		this.recordGroupDao = groupDao;
		this.groupRoleDao = groupRoleDao;
	}

	public List<String> getSuggestions(String field, String text, boolean checkRecordGroup) {
		
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion(field, createSuggestionBuilder(field, text, checkRecordGroup));
		searchSourceBuilder.suggest(suggestBuilder);
		
		SearchRequest request = new SearchRequest("places");
		request.source(searchSourceBuilder);
		
		List<String> suggestions = new ArrayList<String>();
		
		Suggest suggest;
		try {
			suggest = client.search(request, RequestOptions.DEFAULT).getSuggest();
		} catch (IOException e) {
			logger.error("Failed to execute suggestion query for text: " + text, e);
			return suggestions;
		}

		CompletionSuggestion suggestion = suggest.getSuggestion(field); 
		for (CompletionSuggestion.Entry entry : suggestion.getEntries()) { 
		    for (CompletionSuggestion.Entry.Option option : entry) {
		    	String suggestionText = option.getText().string();
		        if (!suggestions.contains(suggestionText)) suggestions.add(suggestionText);
		    }
		}

		return suggestions;
	}
	
	private CompletionSuggestionBuilder createSuggestionBuilder(String field, String text, boolean checkRecordGroup) {
		
		CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion(field).text(text).size(size);
			
		if (checkRecordGroup) {
			List<CategoryQueryContext> contexts = new ArrayList<CategoryQueryContext>();
			for (String recordGroup : getAccessibleRecordGroups()) {
				contexts.add(CategoryQueryContext.builder().setCategory(recordGroup).build());
			}
			
			Map<String, List<? extends ToXContent>> map = new HashMap<String, List<? extends ToXContent>>();
			map.put("recordGroupId", contexts);
			
			suggestionBuilder.contexts(map);
		}
		
		return suggestionBuilder;
	}
	
	private Set<String> getAccessibleRecordGroups() {
		
		Set<String> groupIds = new HashSet<String>();
		groupIds.add("none");
		
		List<RecordGroup> groups = recordGroupDao.findByShowPlaces(true);
		for (RecordGroup group : groups) {
			groupIds.add(group.getId());
		}
		
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		if (user != null) {		
			for (GroupRole role : groupRoleDao.findByUserId(user.getId())) {
				groupIds.add(role.getGroupId());
			}
		}
		
		return groupIds;
	}
}
