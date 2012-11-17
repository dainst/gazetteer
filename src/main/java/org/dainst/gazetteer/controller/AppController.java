package org.dainst.gazetteer.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppController {
	
	@Value("${baseUri}")
	private String baseUri;

	@RequestMapping(value="/")
	public String index() {
		return "redirect:/app/";
	}
	
	@RequestMapping(value="/app/")
	public String app(ModelMap model) {
		model.addAttribute("baseUri",baseUri);
		return "app/index";
	}
	
	@RequestMapping(value="/app/{view}.html")
	public String app(@PathVariable String view, ModelMap model) {
		model.addAttribute("baseUri",baseUri);
		return "app/" + view;
	}
	
	@RequestMapping(value="/app/partials/{view}.html")
	public String appPartials(@PathVariable String view, ModelMap model) {
		model.addAttribute("baseUri",baseUri);
		return "app/partials/" + view;
	}
	
}
