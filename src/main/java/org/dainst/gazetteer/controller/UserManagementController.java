package org.dainst.gazetteer.controller;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.UserPasswordChangeRequestRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.User;
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
	private UserRepository userRepository;
	
	@Autowired
	private UserPasswordChangeRequestRepository userPasswordChangeRequestRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private MailService mailService;
	
	@Value("${baseUri}")
	private String baseUri;
	
	private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);
	
	private int usersPerPage = 15;
	
	@RequestMapping(value="/login")
	public String getLogin(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
		return "login";		
	}
	
	@RequestMapping(value="/loginfailed")
	public String loginerror(ModelMap model) {
		model.addAttribute("error", "true");
		return "login"; 
	}
	
	@RequestMapping(value="/register")
	public String register(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
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

		if (userRepository.findByUsername(username) != null)
			return returnRegisterFailure("usernameExists", request, r, model);
		
		if (firstname.equals(""))
			return returnRegisterFailure("missingFirstname", request, r, model);
		
		if (lastname.equals(""))
			return returnRegisterFailure("missingLastname", request, r, model);
		
		if (userRepository.findByEmail(email) != null)
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
		
		userRepository.save(user);
		
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
			return "home";
		}
	}
	
	@RequestMapping(value="/redirect")
	public String redirect(@RequestParam(required=false) String r) {
		
		if (r == null || r.equals(""))
			return "home";
		else 
			return "redirect:app/#!/" + r;
	}
		
	@RequestMapping(value="/userManagement")
	public String getUserManagement(@RequestParam(required=false) String sort, @RequestParam(required=false) boolean isDescending, @RequestParam(required=false) Integer page, ModelMap model) {
		
		List<User> users = null;

		if (sort == null || sort.equals("")) {
			if (isDescending)
				users = (List<User>) userRepository.findAll(new Sort(Sort.Direction.DESC, "enabled"));
			else
				users = (List<User>) userRepository.findAll(new Sort(Sort.Direction.ASC, "enabled"));				
		}
		else {
			switch (sort) {
			case "username":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.UsernameComparator()));
				else
					Collections.sort(users, new User.UsernameComparator());					
				break;
			case "firstname":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.FirstnameComparator()));
				else
					Collections.sort(users, new User.FirstnameComparator());
				break;
			case "lastname":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.LastnameComparator()));
				else
					Collections.sort(users, new User.LastnameComparator());
				break;
			case "institution":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.InstitutionComparator()));
				else
					Collections.sort(users, new User.InstitutionComparator());
				break;
			case "email":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.EmailComparator()));
				else
					Collections.sort(users, new User.EmailComparator());
				break;
			case "lastLogin":
				if (isDescending)
					users = (List<User>) userRepository.findAll(new Sort(Sort.Direction.ASC, "lastLogin"));
				else
					users = (List<User>) userRepository.findAll(new Sort(Sort.Direction.DESC, "lastLogin"));
				break;
			case "registrationDate":
				if (isDescending)
					users = (List<User>) userRepository.findAll(new Sort(Sort.Direction.ASC, "registrationDate"));
				else
					users = (List<User>) userRepository.findAll(new Sort(Sort.Direction.DESC, "registrationDate"));
				break;
			case "admin":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.AdminComparator()));
				else
					Collections.sort(users, new User.AdminComparator());
				break;
			case "reisestipendium":
				users = (List<User>) userRepository.findAll();
				if (isDescending)
					Collections.sort(users, Collections.reverseOrder(new User.ReisestipendiumComparator()));
				else
					Collections.sort(users, new User.ReisestipendiumComparator());
				break;
			}
		}
		
		int pages = users.size() / usersPerPage + 1;
		
		if (page == null || page < 0)
			page = 0;
		
		if (page >= pages)
			page = pages - 1;
		
		int toIndex = page * usersPerPage + usersPerPage;
		if (toIndex > users.size())
			toIndex = users.size();
		
		users = users.subList(page * usersPerPage, toIndex);
		
		model.addAttribute("page", page);
		model.addAttribute("pages", pages);
		model.addAttribute("users", users);		
		model.addAttribute("isDescending", isDescending);
		model.addAttribute("lastSorting", sort);
		
		return "userManagement";
	}
	
	@RequestMapping(value="/editUser")
	public String getEditUser(@RequestParam(required=true) String username, @RequestParam(required=false) String r, ModelMap model) {
		
		boolean adminEdit = isAdminEdit();		
		boolean userEdit = isUserEdit(username);
		
		if (!userEdit && !adminEdit)
			return "home";
		
		User user = userRepository.findByUsername(username);
		if (user == null)
			return "userManagement";
		
		model.addAttribute("user", user);
		model.addAttribute("edit_user_username_value", user.getUsername());
		model.addAttribute("edit_user_firstname_value", user.getFirstname());
		model.addAttribute("edit_user_lastname_value", user.getLastname());
		model.addAttribute("edit_user_institution_value", user.getInstitution());
		model.addAttribute("edit_user_email_value", user.getEmail());
		model.addAttribute("edit_user_activated_value", user.isEnabled());
		model.addAttribute("edit_user_role_admin_value", user.hasRole("ROLE_ADMIN"));
		model.addAttribute("edit_user_role_reisestipendium_value", user.hasRole("ROLE_REISESTIPENDIUM"));
		model.addAttribute("adminEdit", adminEdit);
		model.addAttribute("userEdit", userEdit);
		model.addAttribute("r", r);
		
		return "editUser";
	}
	
	@RequestMapping(value="/checkEditUserForm")
	public String checkEditUserForm(HttpServletRequest request, @RequestParam(required=true) String username,
									@RequestParam(required=false) String r, ModelMap model) {
		
		User user = userRepository.findByUsername(username);
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
		
		if (adminEdit) {
		newUsername = request.getParameter("edit_user_username");
		isEnabled = request.getParameter("edit_user_activated") != null;
		
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		
		if (request.getParameter("edit_user_role_admin") != null)
			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

		if (request.getParameter("edit_user_role_reisestipendium") != null)
			authorities.add(new SimpleGrantedAuthority("ROLE_REISESTIPENDIUM"));
		}
		
		if (userEdit) {
			newPassword = request.getParameter("edit_user_new_password");
			newPasswordConfirmation = request.getParameter("edit_user_new_password_confirmation");			
		}
		
		if (newFirstname.equals(""))
			return returnEditUserFailure("missingFirstname", user, r, adminEdit, userEdit, request, model);
		
		if (newLastname.equals(""))
			return returnEditUserFailure("missingLastname", user, r, adminEdit, userEdit, request, model);
	
		if (!user.getEmail().equals(newEmail) && userRepository.findByEmail(newEmail) != null)
			return returnEditUserFailure("emailExists", user, r, adminEdit, userEdit, request, model);
		
		if (newEmail.equals("") || !newEmail.matches("^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$"))
			return returnEditUserFailure("invalidEmail", user, r, adminEdit, userEdit, request, model);
		
		if (adminEdit) {
			if (newUsername.equals(""))
				return returnEditUserFailure("missingUsername", user, r, adminEdit, userEdit, request, model);

			if (!username.equals(newUsername) && userRepository.findByUsername(newUsername) != null)
				return returnEditUserFailure("usernameExists", user, r, adminEdit, userEdit, request, model);
		}
		
		if (userEdit) {
			if (!newPassword.equals("") && (newPassword.length() < 6 || newPassword.length() > 30))
				return returnEditUserFailure("passwordLength", user, r, adminEdit, userEdit, request, model);
			
			if (!(newPassword.equals("") && newPasswordConfirmation.equals("")) && !newPassword.equals(newPasswordConfirmation))
				return returnEditUserFailure("passwordInequality", user, r, adminEdit, userEdit, request, model);
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
		
		userRepository.save(user);
	
		if (r != null && r.equals("userManagement") && adminEdit)
			return getUserManagement(null, false, 0, model);
		else if (r != null && !r.equals(""))
			return "redirect:app/#!/" + r;
		else
			return "home";
	}
	
	@RequestMapping(value="/passwordChangeRequest")
	public String getPasswordChangeRequest(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
		return "passwordChangeRequest";		
	}
	
	@RequestMapping(value="/checkPasswordChangeRequestForm")
	public String checkPasswordChangeRequestForm(HttpServletRequest request, @RequestParam(required=false) String r,
												 RedirectAttributes redirectAttributes, ModelMap model) {
		
		String username = request.getParameter("password_change_request_username");
		
		if (username.equals(""))
			return returnPasswordChangeRequestFailure("missingUsername", r, model);

		User user = userRepository.findByUsername(username);
		if (user == null)
			return returnPasswordChangeRequestFailure("userNotFound", r, model);
	
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestRepository.findByUserId(user.getId());
		if (changeRequest != null) {
			Date changeRequestDate = changeRequest.getRequestDate();
		    if (TimeUnit.HOURS.convert(new Date().getTime() - changeRequestDate.getTime(), TimeUnit.MILLISECONDS) > 23)
		    	userPasswordChangeRequestRepository.delete(changeRequest);
		    else
		    	return returnPasswordChangeRequestFailure("requestExists", r, model);			
		}
		
		SecureRandom random = new SecureRandom();
		String resetKey = new BigInteger(130, random).toString(32);
	
		changeRequest = new UserPasswordChangeRequest();
		changeRequest.setUserId(user.getId());		
		changeRequest.setResetKey(passwordEncoder.encode(resetKey));
		changeRequest.setRequestDate(new Date());		
		userPasswordChangeRequestRepository.save(changeRequest);
		
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
			return "home";
		}
	}
	
	@RequestMapping(value="/changePassword")
	public String getChangePassword(@RequestParam(required=true) String userid, @RequestParam(required=true) String key, ModelMap model) {
		
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestRepository.findByUserId(userid);
		User user = userRepository.findById(userid);
		
		if (changeRequest == null || !passwordEncoder.matches(key, changeRequest.getResetKey()))
			return "home";
		
		Date changeRequestDate = changeRequest.getRequestDate();
	    if (TimeUnit.HOURS.convert(new Date().getTime() - changeRequestDate.getTime(), TimeUnit.MILLISECONDS) > 23) {
	    	userPasswordChangeRequestRepository.delete(changeRequest);
	    	return "home";
	    }
	    
	    model.addAttribute("user", user);
	    
	    return "changePassword";		
	}
	
	@RequestMapping(value="/checkChangePasswordForm")
	public String checkChangePasswordForm(HttpServletRequest request, @RequestParam(required=true) String userid, ModelMap model) {
		
		String newPassword = request.getParameter("change_password_password");
		String newPasswordConfirmation = request.getParameter("change_password_password_confirmation");
		
		User user = userRepository.findById(userid);
		
		if (newPassword.length() < 6 || newPassword.length() > 30)
			return returnChangePasswordFailure("passwordLength", user, model);
		
		if (!newPassword.equals(newPasswordConfirmation))
			return returnChangePasswordFailure("passwordInequality", user, model);
		
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
		
		UserPasswordChangeRequest changeRequest = userPasswordChangeRequestRepository.findByUserId(userid);
		userPasswordChangeRequestRepository.delete(changeRequest);
		
		model.addAttribute("successMessage", "changePassword");
		
		return "home";
	}	
	
	private String returnRegisterFailure(String failureType, HttpServletRequest request, String r, ModelMap model) {
		
		model.addAttribute("register_username_value", request.getParameter("register_username"));
		model.addAttribute("register_firstname_value", request.getParameter("register_firstname"));
		model.addAttribute("register_lastname_value", request.getParameter("register_lastname"));
		model.addAttribute("register_institution_value", request.getParameter("register_institution"));
		model.addAttribute("register_email_value", request.getParameter("register_email"));
		model.addAttribute("r", r);
		model.addAttribute("failure", failureType);
		
		return "register";
	}
	
	private String returnEditUserFailure(String failureType, User user, String r, boolean adminEdit, boolean userEdit,
										 HttpServletRequest request, ModelMap model) {
		
		model.addAttribute("edit_user_username_value", request.getParameter("edit_user_username"));
		model.addAttribute("edit_user_firstname_value", request.getParameter("edit_user_firstname"));
		model.addAttribute("edit_user_lastname_value", request.getParameter("edit_user_lastname"));
		model.addAttribute("edit_user_institution_value", request.getParameter("edit_user_institution"));
		model.addAttribute("edit_user_email_value", request.getParameter("edit_user_email"));
		model.addAttribute("edit_user_activated_value", request.getParameter("edit_user_activated") != null);
		model.addAttribute("edit_user_role_admin_value", request.getParameter("edit_user_role_admin") != null);
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
				
		List<User> users = (List<User>) userRepository.findAll();
				
		for (User user : users) {
			if (user.hasRole("ROLE_ADMIN")) {
				mailService.sendMail(user.getEmail(), subject, content);
				logger.info("Sending admin notification mail to " + user.getUsername() + " / " + user.getEmail());
			}
		}
	}
}