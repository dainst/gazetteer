package org.dainst.gazetteer.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RecordGroupController {
	
	@Autowired
	private RecordGroupRepository recordGroupDao;
	
	@Autowired
	private UserRepository userDao;
	
	@Autowired
	private PlaceRepository placeDao;
	
	
	@RequestMapping(value="/recordGroupManagement")
	public String getRecordGroupManagement(@RequestParam(required=false) String deleteRecordGroupId, ModelMap model) {
	
		List<RecordGroup> recordGroups = (List<RecordGroup>) recordGroupDao.findAll();
		
		if (deleteRecordGroupId != null && !deleteRecordGroupId.isEmpty()) {
			RecordGroup recordGroup = recordGroupDao.findOne(deleteRecordGroupId);			
			List<Place> assignedPlaces = placeDao.findByRecordGroupIdAndDeletedIsFalse(deleteRecordGroupId);		
			if (assignedPlaces.size() == 0) {
				List<User> members = userDao.findByRecordGroupIds(deleteRecordGroupId);
				for (User member : members) {
					member.getRecordGroupIds().remove(deleteRecordGroupId);
					userDao.save(member);
				}
				recordGroupDao.delete(recordGroup);
				model.addAttribute("deletedRecordGroup", recordGroup.getName());
			}
		}
		
		recordGroups = (List<RecordGroup>) recordGroupDao.findAll();
		
		Map<String, Integer> recordGroupMembers = new HashMap<String, Integer>();
		Map<String, Integer> recordGroupPlaces = new HashMap<String, Integer>();
		for (RecordGroup recordGroup : recordGroups) {
			List<User> members = userDao.findByRecordGroupIds(recordGroup.getId());
			List<Place> assignedPlaces = placeDao.findByRecordGroupIdAndDeletedIsFalse(recordGroup.getId());

			recordGroupMembers.put(recordGroup.getId(), members.size());
			recordGroupPlaces.put(recordGroup.getId(), assignedPlaces.size());
		}
		
		model.addAttribute("recordGroups", recordGroups);
		model.addAttribute("recordGroupMembers", recordGroupMembers);
		model.addAttribute("recordGroupPlaces", recordGroupPlaces);
		
		return "recordGroupManagement";
	}
	
	@RequestMapping(value="/checkCreateRecordGroupForm")
	public String checkCreateRecordGroupForm(HttpServletRequest request, ModelMap model) {
		
		String groupName = request.getParameter("group_name");
		
		if (!groupName.isEmpty()) {			
			if (recordGroupDao.findByName(groupName) != null)
				model.addAttribute("failure", "groupNameAlreadyExists");				
			else {
				RecordGroup recordGroup = new RecordGroup(groupName);
				recordGroupDao.save(recordGroup);
				model.addAttribute("createdRecordGroup", recordGroup.getName());
			}
		}		
		
		return getRecordGroupManagement(null, model);
	}
}