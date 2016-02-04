package org.dainst.gazetteer.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.elasticsearch.action.suggest.SuggestRequestBuilder;
import org.elasticsearch.action.suggest.SuggestResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.security.core.context.SecurityContextHolder;

public class ElasticSearchSuggestionQuery {
	
	private SuggestRequestBuilder suggestRequestBuilder;
	
	private RecordGroupRepository recordGroupDao;
	
	private GroupRoleRepository groupRoleDao;

	private static int size = 7;

	public ElasticSearchSuggestionQuery(Client client, RecordGroupRepository groupDao, GroupRoleRepository groupRoleDao) {
		suggestRequestBuilder = client.prepareSuggest("gazetteer");
		
		this.recordGroupDao = groupDao;
		this.groupRoleDao = groupRoleDao;
	}

	public List<String> getSuggestions(String field, String text, boolean checkRecordGroup) {

		CompletionSuggestionBuilder builder = new CompletionSuggestionBuilder("suggestions")
			.field(field)
			.text(text)
			.size(size);

		if (checkRecordGroup)
			builder.addCategory("recordGroupId", getAccessibleRecordGroups());
		
		SuggestResponse response = suggestRequestBuilder.addSuggestion(builder).execute().actionGet();

		List<String> suggestions = new ArrayList<String>();

		if (response != null && response.getSuggest() != null && response.getSuggest().getSuggestion("suggestions") != null 
				&& response.getSuggest().getSuggestion("suggestions").getEntries() != null && response.getSuggest().getSuggestion("suggestions").getEntries().size() > 0) {

			Iterator<? extends Suggest.Suggestion.Entry.Option> iterator = response.getSuggest().getSuggestion("suggestions").getEntries().get(0).getOptions().iterator();

			while (iterator.hasNext()) {
				suggestions.add(iterator.next().getText().toString());
			}
		}

		return suggestions;
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
