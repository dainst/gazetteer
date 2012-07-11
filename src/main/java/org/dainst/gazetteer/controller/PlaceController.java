package org.dainst.gazetteer.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;


@Controller
public class PlaceController {

	@Autowired
	private PlaceDao placeDao;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@RequestMapping(value="/place", method=RequestMethod.GET)
	public ModelAndView listPlaces(@RequestParam(defaultValue="50") int limit,
			@RequestParam(defaultValue="0") int offset,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		String language = requestContext.getLocale().getLanguage();
		
		List<Place> places = placeDao.list(limit, offset);
		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		mav.addObject("baseUri", baseUri);
		mav.addObject("language", language);
		return mav;
		
	}
	
	
	// REST-Interface for single places

	@RequestMapping(value="/place/{placeId}", method=RequestMethod.GET)
	public ModelAndView getPlace(@PathVariable long placeId,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		String language = requestContext.getLocale().getLanguage();

		Place place = placeDao.get(placeId);
		if (place != null) {
			ModelAndView mav = new ModelAndView("place/get");
			mav.addObject(place);
			mav.addObject("baseUri", baseUri);
			mav.addObject("language", language);
			return mav;
		}
		
		response.setStatus(404);
		return null;

	}
	
	@RequestMapping(value="/place", method=RequestMethod.POST)
	public void createPlace(@RequestBody Place place,
			HttpServletResponse response) {
		
		place = placeDao.save(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
	}
	
	@RequestMapping(value="/place/{placeId}", method=RequestMethod.PUT)
	public void updateOrCreatePlace(@RequestBody Place place, 
			@PathVariable long placeId,
			HttpServletResponse response) {
		
		place.setId(placeId);
		place = placeDao.save(place);
		
		response.setStatus(204);
		
	}
	
	@RequestMapping(value="/place/{placeId}", method=RequestMethod.DELETE)
	public void deletePlace(@PathVariable long placeId,
			HttpServletResponse response) {
		
		if(placeDao.delete(placeId) != 0) {
			response.setStatus(204);
		} else {
			response.setStatus(404);
		}
		
	}

}
