package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Value("${version}")
	private String version;
	
	
	@RequestMapping(value="/recordGroupManagement")
	public String getRecordGroupManagement(@RequestParam(required=false) String deleteRecordGroupId, ModelMap model) {
		
		List<RecordGroup> recordGroups = null;
		Map<String, String> groupRights = new HashMap<String, String>();
		if (isAdminEdit()) {
			recordGroups = (List<RecordGroup>) recordGroupDao.findAll();
			for (RecordGroup group : recordGroups) {
				groupRights.put(group.getId(), "admin");
			}
		}
		else {
			recordGroups = new ArrayList<RecordGroup>();
			User user = getUser();
			
			List<GroupRole> groupRoles = groupRoleDao.findByUserId(user.getId());			
			for (GroupRole role : groupRoles) {
				RecordGroup group = recordGroupDao.findOne(role.getGroupId());
				recordGroups.add(group);
				groupRights.put(group.getId(), role.getRoleType());
			}
		}		
		
		if (deleteRecordGroupId != null && !deleteRecordGroupId.isEmpty()) {
			RecordGroup recordGroup = recordGroupDao.findOne(deleteRecordGroupId);
			long placeCount = placeDao.getCountByRecordGroupIdAndDeletedIsFalse(deleteRecordGroupId);		
			if (placeCount == 0) {
				List<GroupRole> groupRoles = groupRoleDao.findByGroupId(deleteRecordGroupId);
				for (GroupRole role : groupRoles) {
					groupRoleDao.delete(role);
				}
				recordGroupDao.delete(recordGroup);
				model.addAttribute("deletedRecordGroup", recordGroup.getName());
			}
		}
		
		Map<String, Long> recordGroupMembers = new HashMap<String, Long>();
		Map<String, Long> recordGroupPlaces = new HashMap<String, Long>();
		for (RecordGroup recordGroup : recordGroups) {
			Long memberCount = groupRoleDao.getCountByGroupId(recordGroup.getId());
			Long placeCount = placeDao.getCountByRecordGroupIdAndDeletedIsFalse(recordGroup.getId());

			recordGroupMembers.put(recordGroup.getId(), memberCount);
			recordGroupPlaces.put(recordGroup.getId(), placeCount);
		}
		
		model.addAttribute("recordGroups", recordGroups);
		model.addAttribute("recordGroupMembers", recordGroupMembers);
		model.addAttribute("recordGroupPlaces", recordGroupPlaces);
		model.addAttribute("groupRights", groupRights);
		model.addAttribute("version", version);
		
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
	
	@RequestMapping(value="/recordGroupUserManagement")
	public String getRecordGroupUserManagement(@RequestParam(required=true) String groupId, ModelMap model) {

		RecordGroup group = recordGroupDao.findOne(groupId);		
		List<GroupRole> roles = groupRoleDao.findByGroupId(groupId);
		List<User> users = new ArrayList<User>();
		Map<String, GroupRole> roleMap = new HashMap<String, GroupRole>();
		
		for (GroupRole role : roles) {
			users.add(userDao.findOne(role.getUserId()));
			roleMap.put(role.getUserId(), role);
		}		
		
		model.addAttribute("recordGroup", group);
		model.addAttribute("users", users);
		model.addAttribute("roles", roleMap);
		model.addAttribute("version", version);
		
		return "recordGroupUserManagement";
	}
	
	@RequestMapping(value="/checkRecordGroupUserForm")
	public String checkRecordGroupUserForm(HttpServletRequest request, @RequestParam(required=true) String groupId,
			@RequestParam(required=true) String userId, ModelMap model) {
		
		GroupRole editorRole = groupRoleDao.findByGroupIdAndUserId(groupId, getUser().getId());
		if (!isAdminEdit() && (editorRole == null || !editorRole.getRoleType().equals("admin")))
			return "redirect:app/#!/home";
		
		GroupRole role = groupRoleDao.findByGroupIdAndUserId(groupId, userId);
		
		if (role == null)
			throw new IllegalStateException("No group role found for groupId " + groupId + " and userId " + userId);
		
		String roleType = request.getParameter("access");
		role.setRoleType(roleType);
		
		groupRoleDao.save(role);
		
		return getRecordGroupUserManagement(groupId, model);
	}
	
	private boolean isAdminEdit() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority.getAuthority().equals("ROLE_ADMIN")) {
				return true;
			}
		}
		
		return false;
	}
	
	private User getUser() {
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			return (User) principal;
		else
			return null;
	}
}