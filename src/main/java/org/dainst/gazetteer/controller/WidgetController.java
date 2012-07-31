package org.dainst.gazetteer.controller;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WidgetController {
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Value("${googleMapsApiKey}")
	private String googleMapsApiKey;
	
	@Autowired
	private PlaceDao placeDao;

	@RequestMapping(value="/widget/lib.js")
	public ModelAndView getLibJs() {
		
		ModelAndView mav = new ModelAndView("widget/lib");
		mav.addObject("baseUri", baseUri);
		
		return mav;
		
	}
	
	@RequestMapping(value="/widget/show.js")
	public ModelAndView showPlace(
			@RequestParam String callback,
			@RequestParam long id,
			@RequestParam(defaultValue="150") int mapHeight) {
		
		Place place = placeDao.get(id);
		
		ModelAndView mav = new ModelAndView("widget/show");
		mav.addObject("place", place);
		mav.addObject("callback", callback);
		mav.addObject("baseUri", baseUri);
		mav.addObject("mapHeight", mapHeight);
		mav.addObject("googleMapsApiKey", googleMapsApiKey);
		
		return mav;
		
	}
	
}
