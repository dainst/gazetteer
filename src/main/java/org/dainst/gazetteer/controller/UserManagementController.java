package org.dainst.gazetteer.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.dao.UserPasswordChangeRequestRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.UserPasswordChangeRequest;
import org.dainst.gazetteer.helpers.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class UserManagementController {
	
	@Autowired
	private UserRepository userDao;
	
	@Autowired
	private UserPasswordChangeRequestRepository userPasswordChangeRequestDao;
	
	@Autowired
	private RecordGroupRepository recordGroupDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private MailService mailService;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Value("${version}")
	private String version;
	
	private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
	
	private int usersPerPage = 15;

	
	@RequestMapping(value="/login")
	public String getLogin(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
		model.addAttribute("version", version);
		return "login";		
	}
	
	@RequestMapping(value="/loginfailed")
	public String loginerror(ModelMap model) {
		model.addAttribute("error", "true");
		model.addAttribute("version", version);
		return "login"; 
	}
	
	@RequestMapping(value="/register")
	public String register(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
		model.addAttribute("version", version);
		return "register";
	}
	
	@RequestMapping(value="/checkRegisterForm")
	public String checkRegisterForm(HttpServletRequest request, @RequestParam(required=false) String r,
									RedirectAttributes redirectAttributes, ModelMap model) {
		
		String username = request.getParameter("register_username");
		String firstname = request.getParameter("register_firstname");
		String lastname = request.getParameter("register_lastname");
		String institution = request.getParameter("register_institution");
		String email = request.getParameter("register_email");
		String password = request.getParameter("register_password");
		String passwordConfirmation = request.getParameter("register_password_confirmation");
		
		if (username.equals(""))
			return returnRegisterFailure("missingUsername", request, r, model);

		if (userDao.findByUsername(username) != null)
			return returnRegisterFailure("usernameExists", request, r, model);
		
		if (firstname.equals(""))
			return returnRegisterFailure("missingFirstname", request, r, model);
		
		if (lastname.equals(""))
			return returnRegisterFailure("missingLastname", request, r, model);
		
		if (userDao.findByEmail(email) != null)
			return returnRegisterFailure("emailExists", request, r, model);
		
		if (email.equals("") || !email.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"))
			return returnRegisterFailure("invalidEmail", request, r, model);
		
		if (password.length() < 6 || password.length() > 30)
			return returnRegisterFailure("passwordLength", request, r, model);
		
		if (!password.equals(passwordConfirmation))
			return returnRegisterFailure("passwordInequality", request, r, model);
		
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

		String encodedPassword = passwordEncoder.encode(password);
		
		User user = new User(username, firstname, lastname, institution, email, encodedPassword, new Date(), authorities);
		
		userDao.save(user);
		
		String link = baseUri + "editUser?username=" + username + "&r=userManagement";
		RequestContext context = new RequestContext(request);
		String subject = context.getMessageSource().getMessage("mail.adminRegistrationNotification.subject", new Object[] { username }, Locale.ENGLISH);
		String content = context.getMessageSource().getMessage("mail.adminRegistrationNotification.content", new Object[] { username, link }, Locale.ENGLISH);
				
		try {
			sendMailToAdmins(subject, content);					
		} catch (MessagingException e) {
			logger.warn("Could not send notification mails to admins", e);
		}
		
		if (r != null && !r.equals("")) {
			redirectAttributes.addFlashAttribute("successMessage", "register");
			return "redirect:app/#!/" + r;
		}
		else {
			model.addAttribute("successMessage", "register");
			model.addAttribute("version", version);
			return "redirect:app/#!/home";
		}
	}
	
	@RequestMapping(value="/redirect")
	public String redirect(@RequestParam(required=false) String r) {
		
		if (r == null || r.equals(""))
			r = "home";
		
		return "redirect:app/#!/" + r;
	}
		
	@RequestMapping(value="/userManagement")
	public String getUserManagement(@RequestParam(required=false) String sort, @RequestParam(required=false) boolean isDescending,
									@RequestParam(required=false) Integer page, @RequestParam(required=false) String showUser,
									@RequestParam(required=false) boolean deleteUser, @RequestParam(required=false) String deleteUserId,
									ModelMap model) {
		
		if (deleteUser) {			
			User user = userDao.findById(deleteUserId);
			if (user != null && !user.isEnabled()) {			
				userDao.delete(user);
				model.addAttribute("userDeleted", user.getUsername());
			}	
		}
		
		List<User> users = null;
		int pages;

		if (showUser != null && !showUser.equals("")) {
			
			User userToShow = userDao.findById(showUser);
			
			if (userToShow == null)
				return "redirect:app/#!/home";
			
			users = (List<User>) userDao.findAll();
			Collections.sort(users, new User.UsernameComparator());	
			
			pages = (users.size() + usersPerPage - 1) / usersPerPage;
			List<User> pageUsers;			
			for (int i = 0; i < pages; i++) {
				
				int toIndex = i * usersPerPage + usersPerPage;
				if (toIndex > users.size())
					toIndex = users.size();

					pageUsers = users.subList(i * usersPerPage, toIndex);
	 
				for (User u : pageUsers) {
					if (u.getId().equals(userToShow.getId())) {
						users = pageUsers;
						page = i;
						break;
					}
				}
				
				if (users.equals(pageUsers))
					break;
			}
			
			sort = "username";
			
		} else {
		
			if (sort == null || sort.equals("")) {
				if (isDescending)
					users = (List<User>) userDao.findAll(new Sort(Sort.Direction.DESC, "enabled"));
				else
					users = (List<User>) userDao.findAll(new Sort(Sort.Direction.ASC, "enabled"));				
			}
			else {
				switch (sort) {
				case "username":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.UsernameComparator()));
					else
						Collections.sort(users, new User.UsernameComparator());					
					break;
				case "firstname":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.FirstnameComparator()));
					else
						Collections.sort(users, new User.FirstnameComparator());
					break;
				case "lastname":
				users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.LastnameComparator()));
					else
						Collections.sort(users, new User.LastnameComparator());
					break;
				case "institution":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.InstitutionComparator()));
					else
						Collections.sort(users, new User.InstitutionComparator());
					break;
				case "email":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.EmailComparator()));
					else
						Collections.sort(users, new User.EmailComparator());
					break;
				case "lastLogin":
					if (isDescending)
						users = (List<User>) userDao.findAll(new Sort(Sort.Direction.ASC, "lastLogin"));
					else
						users = (List<User>) userDao.findAll(new Sort(Sort.Direction.DESC, "lastLogin"));
					break;
				case "registrationDate":
					if (isDescending)
						users = (List<User>) userDao.findAll(new Sort(Sort.Direction.ASC, "registrationDate"));
					else
						users = (List<User>) userDao.findAll(new Sort(Sort.Direction.DESC, "registrationDate"));
					break;
				case "admin":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.AdminComparator()));
					else
						Collections.sort(users, new User.AdminComparator());
				break;
				case "editor":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.EditorComparator()));
					else
						Collections.sort(users, new User.EditorComparator());
				break;
				case "reisestipendium":
					users = (List<User>) userDao.findAll();
					if (isDescending)
						Collections.sort(users, Collections.reverseOrder(new User.ReisestipendiumComparator()));
					else
						Collections.sort(users, new User.ReisestipendiumComparator());
					break;
				}
			}
					
			pages = (users.size() + usersPerPage - 1) / usersPerPage;
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
		}
		
		model.addAttribute("page", page);
		model.addAttribute("pages", pages);
		model.addAttribute("users", users);		
		model.addAttribute("isDescending", isDescending);
		model.addAttribute("lastSorting", sort);
		model.addAttribute("version", version);
		
		return "userManagement";
	}
	
	@RequestMapping(value="/editUser")
	public String getEditUser(@RequestParam(required=true) String username, @RequestParam(required=false) String r, ModelMap model) {
		
		boolean adminEdit = isAdminEdit();		
		boolean userEdit = isUserEdit(username);
		
		if (!userEdit && !adminEdit)
			return "redirect:app/#!/home";
		
		User user = userDao.findByUsername(username);
		if (user == null)
			return "userManagement";
		
		List<RecordGroup> recordGroups = (List<RecordGroup>) recordGroupDao.findAll();
		
		if (adminEdit) {
			
			List<String> adminGroupIds = new ArrayList<String>();
			
			List<GroupRole> groupRoles = groupRoleDao.findByUserId(user.getId());			
			for (GroupRole role : groupRoles) {
				if (role.getRoleType().equals("admin") && adminGroupIds.indexOf(role.getGroupId()) == -1)
					adminGroupIds.add(role.getGroupId());
			}
		
			Map<String, Boolean> recordGroupValues = new HashMap<String, Boolean>();
			for (RecordGroup recordGroup : recordGroups) {
				if (adminGroupIds.contains(recordGroup.getId()))
					recordGroupValues.put(recordGroup.getId(), true);
				else
					recordGroupValues.put(recordGroup.getId(), false);
			}
			model.addAttribute("recordGroupsSize", recordGroups.size());
			model.addAttribute("recordGroupValues", recordGroupValues);
			model.addAttribute("edit_user_activated_value", user.isEnabled());
			model.addAttribute("edit_user_role_admin_value", user.hasRole("ROLE_ADMIN"));
			model.addAttribute("edit_user_role_editor_value", user.hasRole("ROLE_EDITOR"));
			model.addAttribute("edit_user_role_reisestipendium_value", user.hasRole("ROLE_REISESTIPENDIUM"));
		}
		
		model.addAttribute("recordGroups", recordGroups);
		model.addAttribute("user", user);
		model.addAttribute("edit_user_username_value", user.getUsername());
		model.addAttribute("edit_user_firstname_value", user.getFirstname());
		model.addAttribute("edit_user_lastname_value", user.getLastname());
		model.addAttribute("edit_user_institution_value", user.getInstitution());
		model.addAttribute("edit_user_email_value", user.getEmail());
		model.addAttribute("adminEdit", adminEdit);
		model.addAttribute("userEdit", userEdit);
		model.addAttribute("r", r);
		model.addAttribute("version", version);
		
		return "editUser";
	}
	
	@RequestMapping(value="/checkEditUserForm")
	public String checkEditUserForm(HttpServletRequest request, @RequestParam(required=true) String username,
									@RequestParam(required=false) String r, ModelMap model) {
		
		User user = userDao.findByUsername(username);
		if (user == null)
			return "register";
		
		boolean adminEdit = isAdminEdit();		
		boolean userEdit = isUserEdit(username);
		
		String newFirstname = request.getParameter("edit_user_firstname");
		String newLastname = request.getParameter("edit_user_lastname");
		String newInstitution = request.getParameter("edit_user_institution");
		String newEmail = request.getParameter("edit_user_email");
		String newUsername = "";
		String newPassword = "";
		String newPasswordConfirmation = "";
		boolean isEnabled = false;
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		List<RecordGroup> recordGroups = (List<RecordGroup>) recordGroupDao.findAll();
		Map<String, Boolean> recordGroupValues = new HashMap<String, Boolean>();
		
		if (adminEdit) {
			newUsername = request.getParameter("edit_user_username");
			isEnabled = request.getParameter("edit_user_activated") != null;
		
			authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		
			if (request.getParameter("edit_user_role_admin") != null)
				authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
		
			if (request.getParameter("edit_user_role_editor") != null)
				authorities.add(new SimpleGrantedAuthority("ROLE_EDITOR"));

			if (request.getParameter("edit_user_role_reisestipendium") != null)
				authorities.add(new SimpleGrantedAuthority("ROLE_REISESTIPENDIUM"));
			
			List<String> selectedRecordGroupIds = new ArrayList<String>();
			if (request.getParameterValues("edit_group_admins") != null)
				selectedRecordGroupIds = new ArrayList<String>(Arrays.asList(request.getParameterValues("edit_group_admins")));
			for (RecordGroup recordGroup : recordGroups) {
				if (selectedRecordGroupIds.contains(recordGroup.getId())) {
					recordGroupValues.put(recordGroup.getId(), true);
					GroupRole existingRole = groupRoleDao.findByGroupIdAndUserId(recordGroup.getId(), user.getId());
					if (existingRole == null) {
						GroupRole adminRole = new GroupRole();
						adminRole.setGroupId(recordGroup.getId());
						adminRole.setUserId(user.getId());
						adminRole.setRoleType("admin");
						groupRoleDao.save(adminRole);
					} else {
						existingRole.setRoleType("admin");
						groupRoleDao.save(existingRole);
					}
				}
				
				if (!selectedRecordGroupIds.contains(recordGroup.getId())) {
					recordGroupValues.put(recordGroup.getId(), false);
					GroupRole role = groupRoleDao.findByGroupIdAndUserId(recordGroup.getId(), user.getId());
					if (role != null && role.getRoleType().equals("admin"))
						groupRoleDao.delete(role.getId());
				}
			}
		}
		
		if (userEdit) {
			newPassword = request.getParameter("edit_user_new_password");
			newPasswordConfirmation = request.getParameter("edit_user_new_password_confirmation");			
		}
		
		if (newFirstname.equals(""))
			return returnEditUserFailure("missingFirstname", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
		
		if (newLastname.equals(""))
			return returnEditUserFailure("missingLastname", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
	
		if (!user.getEmail().equals(newEmail) && userDao.findByEmail(newEmail) != null)
			return returnEditUserFailure("emailExists", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
		
		if (newEmail.equals("") || !newEmail.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"))
			return returnEditUserFailure("invalidEmail", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
		
		if (adminEdit) {
			if (newUsername.equals(""))
				return returnEditUserFailure("missingUsername", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);

			if (!username.equals(newUsername) && userDao.findByUsername(newUsername) != null)
				return returnEditUserFailure("usernameExists", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
		}
		
		if (userEdit) {
			if (!newPassword.equals("") && (newPassword.length() < 6 || newPassword.length() > 30))
				return returnEditUserFailure("passwordLength", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
			
			if (!(newPassword.equals("") && newPasswordConfirmation.equals("")) && !newPassword.equals(newPasswordConfirmation))
				return returnEditUserFailure("passwordInequality", user, r, adminEdit, userEdit, recordGroups, recordGroupValues, request, model);
		}
		
		user.setFirstname(newFirstname);
		user.setLastname(newLastname);
		user.setInstitution(newInstitution);
		user.setEmail(newEmail);
		
		if (adminEdit) {
			
			if (!user.isEnabled() && isEnabled == true) {
				
				String link = baseUri + "login";
				RequestContext context = new RequestContext(request);
				String subject = context.getMessage("mail.userActivationNotification.subject", new Object[] { });
				String content = context.getMessage("mail.userActivationNotification.content", new Object[] { username, link });
						
				try {
					mailService.sendMail(user.getEmail(), subject, content);
					logger.info("Sending user notification mail to " + user.getUsername() + " / " + user.getEmail());
				} catch (MessagingException e) {
					logger.warn("Could not send notification mail to user " + user.getUsername() + " / " + user.getEmail(), e);
				}
			}
			
			user.setUsername(newUsername);
			user.setEnabled(isEnabled);
			user.setAuthorities(authorities);
		}
		
		if (userEdit && !newPassword.equals(""))
			user.setPassword(passwordEncoder.encode(newPassword));		
		
		userDao.save(user);
		
		model.addAttribute("version", version);
	
		if (r != null && r.equals("userManagement") && adminEdit)
			return getUserManagement(null, false, 0, null, false, null, model);
		else if (r != null && !r.equals(""))
			return "redirect:app/#!/" + r;
		else
			return "redirect:app/#!/home";
	}
	
	@RequestMapping(value="/passwordChangeRequest")
	public String getPasswordChangeRequest(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
		model.addAttribute("version", version);
		return "passwordChangeRequest";		
	}
	
	@RequestMapping(value="/checkPasswordChangeRequestForm")
	public String checkPasswordChangeRequestForm(HttpServletRequest request, @RequestParam(required=false) String r,
												 RedirectAttributes redirectAttributes, ModelMap model) {
		
		String username = request.getParameter("password_change_request_username");
		
		if (username.equals(""))
			return returnPasswordChangeRequestFailure("missingUsername", r, model);

		User user = userDao.findByUsername(username);
		if (user == null)
			return returnPasswordChangeRequestFailure("userNotFound", r, model);
	
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestDao.findByUserId(user.getId());
		if (changeRequest != null) {
			Date changeRequestDate = changeRequest.getRequestDate();
		    if (TimeUnit.HOURS.convert(new Date().getTime() - changeRequestDate.getTime(), TimeUnit.MILLISECONDS) > 23)
		    	userPasswordChangeRequestDao.delete(changeRequest);
		    else
		    	return returnPasswordChangeRequestFailure("requestExists", r, model);			
		}
		
		SecureRandom random = new SecureRandom();
		String resetKey = new BigInteger(130, random).toString(32);
	
		changeRequest = new UserPasswordChangeRequest();
		changeRequest.setUserId(user.getId());		
		changeRequest.setResetKey(passwordEncoder.encode(resetKey));
		changeRequest.setRequestDate(new Date());		
		userPasswordChangeRequestDao.save(changeRequest);
		
		String link = baseUri + "changePassword?userid=" + user.getId() + "&key=" + resetKey;
		RequestContext context = new RequestContext(request);
		String subject = context.getMessage("mail.userPasswordResetNotification.subject", new Object[] { });
		String content = context.getMessage("mail.userPasswordResetNotification.content", new Object[] { link });
				
		try {
			mailService.sendMail(user.getEmail(), subject, content);
			logger.info("Sending password reset mail to " + user.getUsername() + " / " + user.getEmail());
		} catch (MessagingException e) {
			logger.warn("Could not send password reset mail to user " + user.getUsername() + " / " + user.getEmail(), e);
		}
		
		if (r != null && !r.equals("")) {
			redirectAttributes.addFlashAttribute("successMessage", "passwordChangeRequest");
			return "redirect:app/#!/" + r;
		}
		else {
			model.addAttribute("successMessage", "passwordChangeRequest");
			model.addAttribute("version", version);
			return "redirect:app/#!/home";
		}
	}
	
	@RequestMapping(value="/changePassword")
	public String getChangePassword(@RequestParam(required=true) String userid, @RequestParam(required=true) String key, ModelMap model) {
		
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestDao.findByUserId(userid);
		User user = userDao.findById(userid);
		
		if (changeRequest == null || !passwordEncoder.matches(key, changeRequest.getResetKey()))
			return "redirect:app/#!/home";
		
		Date changeRequestDate = changeRequest.getRequestDate();
	    if (TimeUnit.HOURS.convert(new Date().getTime() - changeRequestDate.getTime(), TimeUnit.MILLISECONDS) > 23) {
	    	userPasswordChangeRequestDao.delete(changeRequest);
	    	return "redirect:app/#!/home";
	    }
	    
	    model.addAttribute("user", user);
		model.addAttribute("version", version);
	    
	    return "changePassword";		
	}
	
	@RequestMapping(value="/checkChangePasswordForm")
	public String checkChangePasswordForm(HttpServletRequest request, @RequestParam(required=true) String userid, ModelMap model) {
		
		String newPassword = request.getParameter("change_password_password");
		String newPasswordConfirmation = request.getParameter("change_password_password_confirmation");
		
		User user = userDao.findById(userid);
		
		if (newPassword.length() < 6 || newPassword.length() > 30)
			return returnChangePasswordFailure("passwordLength", user, model);
		
		if (!newPassword.equals(newPasswordConfirmation))
			return returnChangePasswordFailure("passwordInequality", user, model);
		
		user.setPassword(passwordEncoder.encode(newPassword));
		userDao.save(user);
		
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestDao.findByUserId(userid);
		userPasswordChangeRequestDao.delete(changeRequest);
		
		model.addAttribute("successMessage", "changePassword");
		model.addAttribute("version", version);
		
		return "redirect:app/#!/home";
	}

	private String returnRegisterFailure(String failureType, HttpServletRequest request, String r, ModelMap model) {
		
		model.addAttribute("register_username_value", request.getParameter("register_username"));
		model.addAttribute("register_firstname_value", request.getParameter("register_firstname"));
		model.addAttribute("register_lastname_value", request.getParameter("register_lastname"));
		model.addAttribute("register_institution_value", request.getParameter("register_institution"));
		model.addAttribute("register_email_value", request.getParameter("register_email"));
		model.addAttribute("r", r);
		model.addAttribute("failure", failureType);
		model.addAttribute("version", version);
		
		return "register";
	}
	
	private String returnEditUserFailure(String failureType, User user, String r, boolean adminEdit, boolean userEdit,
										 List<RecordGroup> recordGroups, Map<String, Boolean> recordGroupValues,
										 HttpServletRequest request, ModelMap model) {
		
		model.addAttribute("recordGroups", recordGroups);
		model.addAttribute("recordGroupsSize", recordGroups.size());
		model.addAttribute("recordGroupValues", recordGroupValues);
		model.addAttribute("edit_user_username_value", request.getParameter("edit_user_username"));
		model.addAttribute("edit_user_firstname_value", request.getParameter("edit_user_firstname"));
		model.addAttribute("edit_user_lastname_value", request.getParameter("edit_user_lastname"));
		model.addAttribute("edit_user_institution_value", request.getParameter("edit_user_institution"));
		model.addAttribute("edit_user_email_value", request.getParameter("edit_user_email"));
		model.addAttribute("edit_user_activated_value", request.getParameter("edit_user_activated") != null);
		model.addAttribute("edit_user_role_admin_value", request.getParameter("edit_user_role_admin") != null);
		model.addAttribute("edit_user_role_editor_value", request.getParameter("edit_user_role_editor") != null);
		model.addAttribute("edit_user_role_reisestipendium_value", request.getParameter("edit_user_role_reisestipendium") != null);
		model.addAttribute("failure", failureType);
		model.addAttribute("user", user);
		model.addAttribute("r", r);
		model.addAttribute("adminEdit", adminEdit);
		model.addAttribute("userEdit", userEdit);
		
		return "editUser";
	}
	
	private String returnPasswordChangeRequestFailure(String failureType, String r, ModelMap model) {
		
		model.addAttribute("failure", failureType);
		model.addAttribute("r", r);
		
		return "passwordChangeRequest";		
	}
	
	private String returnChangePasswordFailure(String failureType, User user, ModelMap model) {
		
		model.addAttribute("failure", failureType);
		model.addAttribute("user", user);
		
		return "changePassword";		
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
	
	private boolean isUserEdit(String username) {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		return authentication.getName().equals(username);
	}
	
	private void sendMailToAdmins(String subject, String content) throws MessagingException {
				
		List<User> users = (List<User>) userDao.findAll();
				
		for (User user : users) {
			if (user.hasRole("ROLE_ADMIN")) {
				mailService.sendMail(user.getEmail(), subject, content);
				logger.info("Sending admin notification mail to " + user.getUsername() + " / " + user.getEmail());
			}
		}
	}
}