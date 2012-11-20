package org.dainst.gazetteer.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class AppController {
	
	@Value("${baseUri}")
	private String baseUri;

	/*@RequestMapping(value="/")
	public String index() {
		return "redirect:/app/";
	}*/
	
	@RequestMapping(value="/app/")
	public String app(ModelMap model, HttpServletRequest request) {
		model.addAttribute("baseUri",baseUri);
		model.addAttribute("language", new RequestContext(request).getLocale().getLanguage());
		return "app/index";
	}
	
	@RequestMapping(value="/app/{view}.html")
	public String app(@PathVariable String view, ModelMap model, HttpServletRequest request) {
		model.addAttribute("baseUri",baseUri);
		model.addAttribute("language", new RequestContext(request).getLocale().getLanguage());
		return "app/" + view;
	}
	
	@RequestMapping(value="/app/partials/{view}.html")
	public String appPartials(@PathVariable String view, ModelMap model) {
		model.addAttribute("baseUri",baseUri);
		return "app/partials/" + view;
	}
	
}
