package org.dainst.gazetteer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AppController {

	@RequestMapping(value="/")
	public String index() {
		return "redirect:/app/";
	}
	
	@RequestMapping(value="/app/")
	public String app() {
		return "app/index";
	}
	
	@RequestMapping(value="/app/{view}.html")
	public String app(@PathVariable String view) {
		return "app/" + view;
	}
	
	@RequestMapping(value="/app/partials/{view}.html")
	public String appPartials(@PathVariable String view) {
		return "app/partials/" + view;
	}
	
}
