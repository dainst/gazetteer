package org.dainst.gazetteer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {
	
	@RequestMapping(value="/login")
	public String getUser(@RequestParam(required=false) String r, ModelMap model) {
		if (r != null) model.addAttribute("r", r);
		return "login";		
	}
	
	@RequestMapping(value="/loginfailed")
	public String loginerror(ModelMap model) {
		model.addAttribute("error", "true");
		return "login"; 
	}
	
	
}
