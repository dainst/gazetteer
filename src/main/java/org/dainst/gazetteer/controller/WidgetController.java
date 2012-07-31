package org.dainst.gazetteer.controller;

import java.util.ArrayList;

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
			@RequestParam(required=false, value="id") long[] ids,
			@RequestParam(defaultValue="150") int mapHeight,
			@RequestParam(defaultValue="false") boolean showInfo) {
		
		ArrayList<Place> places = new ArrayList<Place>();
		for (int i = 0; i < ids.length; i++) {
			Place place = placeDao.get(ids[i]);
			if(place != null) places.add(place);
		}
		
		ModelAndView mav = new ModelAndView("widget/show");
		mav.addObject("places", places);
		mav.addObject("callback", callback);
		mav.addObject("baseUri", baseUri);
		mav.addObject("mapHeight", mapHeight);
		mav.addObject("showInfo", showInfo);
		mav.addObject("googleMapsApiKey", googleMapsApiKey);
		
		return mav;
		
	}
	
}
