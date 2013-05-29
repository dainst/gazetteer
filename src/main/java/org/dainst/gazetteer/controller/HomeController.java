package org.dainst.gazetteer.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	@Autowired
	private PlaceRepository placeRepository;

	@RequestMapping(value="/")
	public ModelAndView home() {
		
		long time = System.currentTimeMillis();
		
		List<Place> places = placeRepository.findByPrefLocationIsNotNull(new PageRequest(0, 10000));
		
		logger.debug("findOne: {}", System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		
		ModelAndView mav = new ModelAndView("home");
		mav.addObject("places", places);
		
		return mav;
	}
	
	@RequestMapping(value = "/robots.txt", method = RequestMethod.GET)
	@ResponseBody
    public String getRobots(HttpServletRequest request) {
        return "User-agent: *\nDisallow: /";
    }
	
}
