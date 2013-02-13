package org.dainst.gazetteer.controller;

import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {
	
	@Autowired
	private PlaceRepository placeRepository;

	@RequestMapping(value="/")
	public ModelAndView home() {
		List<Place> places = placeRepository.findByPrefLocationIsNotNull();
		
		ModelAndView mav = new ModelAndView("home");
		mav.addObject("places", places);
		
		return mav;
	}
	
}
