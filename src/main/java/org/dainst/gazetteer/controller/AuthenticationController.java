package org.dainst.gazetteer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AuthenticationController {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
	
	/**
	 * Handles login
	 */
	@RequestMapping(value="/login")
	public String getUser() {		
		return "login";		
	}
	
	
}
