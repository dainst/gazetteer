package org.dainst.gazetteer.controller;

import javax.servlet.http.HttpServletRequest;

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
	public ModelAndView getPlace(@PathVariable long placeId,
			HttpServletRequest request) {
		
		Place place = placeDao.get(placeId);
		place.setMainUri(request.getRequestURL().toString());
		return new ModelAndView("place/get", "place", place);
		
	}
	
}
