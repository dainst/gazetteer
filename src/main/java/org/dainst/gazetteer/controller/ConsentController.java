package org.dainst.gazetteer.controller;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class ConsentController {
	
	private static Logger logger = LoggerFactory.getLogger(AppController.class);


	@RequestMapping(value="/consent")
	public String consent(
			@RequestParam(value="redirectTo") String redirectTo,
			HttpServletResponse response
	) {
		logger.debug("Setting consent cookie and redirecting to " + redirectTo);
		Cookie consent_cookie = new Cookie("gazetteer_google_consent", "1");

		response.addCookie(consent_cookie);
		return "redirect:" + redirectTo;
	}
}
