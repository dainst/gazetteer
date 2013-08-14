package org.dainst.gazetteer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Handles http requests (currently only get) for <code>/auth<code>.
 */
@Controller
public class AuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
	/**
	 * Handles login
	 * @return Session a session object, also containing the user and his groups
	 */
	@RequestMapping(value="/user")
	public @ResponseBody User getUser() {
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		logger.debug("User: {}", user);
		return user;
		
	}
	
	
}
