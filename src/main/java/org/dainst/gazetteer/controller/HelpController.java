package org.dainst.gazetteer.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.HelpTextRepository;
import org.dainst.gazetteer.domain.HelpText;
import org.dainst.gazetteer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class HelpController {
	
	@Autowired
	private HelpTextRepository helpTextDao;
	
	@ResponseBody
	@RequestMapping(value = "/help/", method = RequestMethod.GET, produces = "text/plain; charset=utf-8")
	public String getText(HttpServletRequest request) throws Exception {
		
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		
		HelpText helpText =
				helpTextDao.findByLanguageAndLoginNeeded(locale.getISO3Language(), user != null);
		
		if (helpText == null)
			return "";
		else
			return helpText.getText();
	}

	@ResponseBody
	@RequestMapping(value = "/help/{language}/{loginNeeded}", method = RequestMethod.GET, produces = "text/plain; charset=utf-8")
	public String getText(@PathVariable String language, @PathVariable String loginNeeded) throws Exception {
		
		if (!checkForAdminRights())
			return "";
		
		boolean login = Boolean.parseBoolean(loginNeeded);
		
		HelpText helpText =
				helpTextDao.findByLanguageAndLoginNeeded(language, login);
		
		if (helpText == null)
			return "";
		else		
			return helpText.getText();
	}
	
	@ResponseBody
	@RequestMapping(value = "/help/{language}/{loginNeeded}", method = RequestMethod.PUT, produces = "text/plain; charset=utf-8")
	public String updateText(@RequestBody String text,
			@PathVariable String language,
			@PathVariable String loginNeeded) throws Exception {
		
		if (!checkForAdminRights())
			return "";
		
		boolean login = Boolean.parseBoolean(loginNeeded);
		
		HelpText helpText =
				helpTextDao.findByLanguageAndLoginNeeded(language, login);
		
		if (helpText == null) {
			helpText = new HelpText();
			helpText.setLanguage(language);
			helpText.setLoginNeeded(login);
		}
		
		helpText.setText(text);
		
		helpTextDao.save(helpText);
		
		return helpText.getText();
	}
	
	private boolean checkForAdminRights() {
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority.getAuthority().equals("ROLE_ADMIN")) {
				return true;
			}
		}
		
		return false;
	}
	
}
