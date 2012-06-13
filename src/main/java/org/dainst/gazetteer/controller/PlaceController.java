package org.dainst.gazetteer.controller;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Controller
@RequestMapping(value="/place/**")
public class PlaceController {

	@Autowired
	private PlaceDao placeDao;
	
	@RequestMapping(value="/{placeId}", method=RequestMethod.GET)
	public ModelAndView getPlace(@PathVariable long placeId) {
		
		Place place = placeDao.get(placeId);
		return new ModelAndView("place/get", "place", place);
		
	}
	
}
