package org.dainst.gazetteer.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.helpers.LocalizedLanguagesHelper;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	@Value("${googleMapsApiKey}")
	private String googleMapsApiKey;
	
	@Autowired
	LocalizedLanguagesHelper langHelper;

	/*@RequestMapping(value="/")
	public String index() {
		return "redirect:/app/";
	}*/
	
	@RequestMapping(value="/app/")
	public String app(ModelMap model, HttpServletRequest request) {
		model.addAttribute("baseUri",baseUri);
		Locale locale = new RequestContext(request).getLocale();
		model.addAttribute("language", locale.getLanguage());
		model.addAttribute("languages", langHelper.getLocalizedLanguages(locale));
		model.addAttribute("googleMapsApiKey", googleMapsApiKey);
		return "app/index";
	}
	
	@RequestMapping(value="/app/{view}.html")
	public String app(@PathVariable String view, ModelMap model, HttpServletRequest request) {
		model.addAttribute("baseUri",baseUri);
		Locale locale = new RequestContext(request).getLocale();
		model.addAttribute("language", locale.getLanguage());
		model.addAttribute("languages", langHelper.getLocalizedLanguages(locale));
		model.addAttribute("googleMapsApiKey", googleMapsApiKey);
		return "app/" + view;
	}
	
	@RequestMapping(value="/app/partials/{view}.html")
	public String appPartials(@PathVariable String view, ModelMap model, HttpServletRequest request) {
		model.addAttribute("baseUri",baseUri);
		Locale locale = new RequestContext(request).getLocale();
		model.addAttribute("language", locale.getLanguage());
		model.addAttribute("languages", langHelper.getLocalizedLanguages(locale));
		return "app/partials/" + view;
	}
	
}