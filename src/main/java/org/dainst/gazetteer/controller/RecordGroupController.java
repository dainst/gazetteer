package org.dainst.gazetteer.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContext;

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
	
	@Autowired
	private MailService mailService;
	
	@Value("${version}")
	private String version;
	
	private int usersPerPage = 10;
	
	private static final Logger logger = LoggerFactory.getLogger(RecordGroupController.class);
	
	
	@RequestMapping(value="/recordGroupManagement")
	public String getRecordGroupManagement(@RequestParam(required=false) String deleteRecordGroupId, ModelMap model) {
		
		List<RecordGroup> recordGroups = null;
		Map<String, String> groupRights = new HashMap<String, String>();
		if (isAdminEdit()) {
			recordGroups = (List<RecordGroup>) recordGroupDao.findAll(new Sort(Sort.Direction.ASC, "creationDate"));
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
		
		if (isAdminEdit() && deleteRecordGroupId != null && !deleteRecordGroupId.isEmpty()) {
			RecordGroup recordGroup = recordGroupDao.findOne(deleteRecordGroupId);
			long placeCount = placeDao.getCountByRecordGroupIdAndDeletedIsFalse(deleteRecordGroupId);
			if (placeCount == 0) {
				List<GroupRole> groupRoles = groupRoleDao.findByGroupId(deleteRecordGroupId);
				for (GroupRole role : groupRoles) {
					groupRoleDao.delete(role);
				}
				recordGroups.remove(recordGroup);
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
		
		if (isAdminEdit()) {
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
		}
		
		return getRecordGroupManagement(null, model);
	}
	
	@RequestMapping(value="/recordGroupUserManagement")
	public String getRecordGroupUserManagement(@RequestParam(required=true) String groupId, @RequestParam(required=false) String sort,
			@RequestParam(required=false) boolean isDescending, @RequestParam(required=false) Integer page, ModelMap model) {

		if (!checkGroupUserManagementAccess(groupId))
			return "redirect:app/#!/home";
		
		RecordGroup group = recordGroupDao.findOne(groupId);		
		List<GroupRole> roles = groupRoleDao.findByGroupId(groupId);
		List<User> users = new ArrayList<User>();
		Map<String, GroupRole> roleMap = new HashMap<String, GroupRole>();
		
		for (GroupRole role : roles) {
			User user = userDao.findOne(role.getUserId());
			if (user != null)
				users.add(user);
			else
				logger.warn("Couldn't find user " + role.getUserId() + " for role " + role.getId());
			roleMap.put(role.getUserId(), role);
		}
		
		if (sort == null || sort.equals(""))
			sort = "username";
		
		switch (sort) {
		case "username":
			if (isDescending)
				Collections.sort(users, Collections.reverseOrder(new User.UsernameComparator()));
			else
				Collections.sort(users, new User.UsernameComparator());					
			break;
		case "firstname":
			if (isDescending)
				Collections.sort(users, Collections.reverseOrder(new User.FirstnameComparator()));
			else
				Collections.sort(users, new User.FirstnameComparator());
			break;
		case "lastname":
			if (isDescending)
				Collections.sort(users, Collections.reverseOrder(new User.LastnameComparator()));
			else
				Collections.sort(users, new User.LastnameComparator());
			break;
		case "institution":
			if (isDescending)
				Collections.sort(users, Collections.reverseOrder(new User.InstitutionComparator()));
			else
				Collections.sort(users, new User.InstitutionComparator());
			break;
		case "email":
			if (isDescending)
				Collections.sort(users, Collections.reverseOrder(new User.EmailComparator()));
			else
				Collections.sort(users, new User.EmailComparator());
			break;
		}
		
		int pages = (users.size() + usersPerPage - 1) / usersPerPage;
		if (pages == 0)
			pages = 1;
		
		if (page == null || page < 0)
			page = 0;
	
		if (page >= pages)
			page = pages - 1;
	
		int toIndex = page * usersPerPage + usersPerPage;
		if (toIndex > users.size())
			toIndex = users.size();
	
		users = users.subList(page * usersPerPage, toIndex);
		
		model.addAttribute("recordGroup", group);
		model.addAttribute("users", users);
		model.addAttribute("roles", roleMap);
		model.addAttribute("isDescending", isDescending);
		model.addAttribute("lastSorting", sort);
		model.addAttribute("page", page);
		model.addAttribute("pages", pages);
		model.addAttribute("version", version);
		
		return "recordGroupUserManagement";
	}
	
	@RequestMapping(value="/checkRecordGroupUserForm")
	public String checkRecordGroupUserForm(HttpServletRequest request, @RequestParam(required=true) String groupId,
			@RequestParam(required=true) String userId, @RequestParam(required=false) String sort,
			@RequestParam(required=false) boolean isDescending, @RequestParam(required=false) Integer page , ModelMap model) {
		
		if (!checkGroupUserManagementAccess(groupId))
			return "redirect:app/#!/home";
		
		User user = userDao.findOne(userId);
		GroupRole role = groupRoleDao.findByGroupIdAndUserId(groupId, userId);
		
		if (role == null)
			throw new IllegalStateException("No group role found for groupId " + groupId + " and userId " + userId);
		
		String roleType = request.getParameter("access");
		role.setRoleType(roleType);
		
		groupRoleDao.save(role);
		
		model.addAttribute("changedUserStatus", user.getUsername());
		
		return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
	}
	
	@RequestMapping(value="/checkAddUserToGroupForm")
	public String checkAddUserToGroupForm(HttpServletRequest request, @RequestParam(required=true) String groupId, @RequestParam(required=false) String sort,
			@RequestParam(required=false) boolean isDescending, @RequestParam(required=false) Integer page, ModelMap model) {
	
		if (!checkGroupUserManagementAccess(groupId))
			return "redirect:app/#!/home";
		
		String emailOrUsername = request.getParameter("email_or_username");
		
		if (emailOrUsername.isEmpty()) {
			model.addAttribute("failure", "noInput");
		
			return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
		}
		
		User user = userDao.findByUsername(emailOrUsername);
		if (user == null)
			user = userDao.findByEmail(emailOrUsername);
		if (user == null) {
			model.addAttribute("emailOrUsername", emailOrUsername);
			model.addAttribute("failure", "notFound");
		
			return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
		}
		
		if (groupRoleDao.findByGroupIdAndUserId(groupId, user.getId()) != null) {
			model.addAttribute("emailOrUsername", emailOrUsername);
			model.addAttribute("failure", "alreadyInGroup");
		
			return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
		}
		
		GroupRole role = new GroupRole();
		role.setUserId(user.getId());
		role.setGroupId(groupId);
		role.setRoleType("limitedRead");
		groupRoleDao.save(role);
		
		model.addAttribute("addedUser", user.getUsername());
		
		return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
	}
	
	@RequestMapping(value="/removeUserFromGroup")
	public String removeUserFromGroup(@RequestParam(required=true) String groupId, @RequestParam(required=true) String userId, @RequestParam(required=false) String sort,
			@RequestParam(required=false) boolean isDescending, @RequestParam(required=false) Integer page, ModelMap model) {
		
		if (!checkGroupUserManagementAccess(groupId))
			return "redirect:app/#!/home";
		
		User user = userDao.findOne(userId);
		GroupRole role = groupRoleDao.findByGroupIdAndUserId(groupId, userId);
		
		if (role == null)
			throw new IllegalStateException("No group role found for groupId " + groupId + " and userId " + userId);
		
		groupRoleDao.delete(role);
		
		model.addAttribute("removedUser", user.getUsername());
		
		return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
	}
	
	@RequestMapping(value="/changeGroupPlaceVisibilityMode")
	public String changeGroupPlaceVisibilityMode(@RequestParam(required=true) String groupId, @RequestParam(required=true) boolean showPlaces, @RequestParam(required=false) String sort,
			@RequestParam(required=false) boolean isDescending, @RequestParam(required=false) Integer page, ModelMap model) {
		
		if (!checkGroupUserManagementAccess(groupId))
			return "redirect:app/#!/home";
		
		RecordGroup group = recordGroupDao.findOne(groupId);
		
		if (group == null )
			throw new IllegalStateException("No group with groupId " + groupId + " could be found.");
		
		group.setShowPlaces(showPlaces);
		recordGroupDao.save(group);
		
		return getRecordGroupUserManagement(groupId, sort, isDescending, page, model);
	}
	
	@RequestMapping(value="/sendRecordGroupContactMail", method=RequestMethod.POST)
	public void sendRecordGroupContactMail(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(required=true) String groupId) {
		
		RecordGroup group = recordGroupDao.findOne(groupId);
		if (group == null)
			throw new IllegalStateException("Record group with id " + groupId + " could not be found.");
		
		StringBuffer messageBuffer = new StringBuffer();
		String line = null;
		BufferedReader reader;
		try {
			reader = request.getReader();
			while ((line = reader.readLine()) != null) {
				if (messageBuffer.length() > 0)
					messageBuffer.append("<br/>");
		    	messageBuffer.append(line);
		    }
		} catch (IOException e) {
			throw new RuntimeException("Failed to read request body", e);
		}	    
	    String message = messageBuffer.toString();		
		
		User user = getUser();
		if (user == null)
			throw new IllegalStateException("Only registered users can send contact mails to group administrators");
		
		String name = user.getFirstname() + " " + user.getLastname();

		List<GroupRole> groupAdminRoles = groupRoleDao.findByGroupIdAndRoleType(groupId, "admin");
		
		RequestContext context = new RequestContext(request);
		String subject = context.getMessage("mail.recordGroupContact.subject", new Object[] { group.getName() });
		String content = "";
		
		if (!name.isEmpty())
			content = context.getMessage("mail.recordGroupContact.content", new Object[] { group.getName(), user.getUsername(), name, user.getInstitution(), user.getEmail(), message })
				+ message;
		
		for (GroupRole role : groupAdminRoles) {
			User admin = userDao.findOne(role.getUserId());
			try {
				mailService.sendMail(admin.getEmail(), subject, content, user.getEmail());
			} catch (MessagingException e) {
				logger.warn("Failed to send contact mail to group admins!", admin.getEmail());
				response.setStatus(500);
			}
		}
		
		response.setStatus(204);
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
	
	private boolean checkGroupUserManagementAccess(String groupId) {
		
		GroupRole editorRole = groupRoleDao.findByGroupIdAndUserId(groupId, getUser().getId());
		
		return (isAdminEdit() || (editorRole != null && editorRole.getRoleType().equals("admin")));
	}
}