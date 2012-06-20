package org.dainst.gazetteer.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class PlaceController {

	@Autowired
	private PlaceDao placeDao;

	@RequestMapping(value="/place/{placeId}", method=RequestMethod.GET)
	public ModelAndView getPlace(@PathVariable long placeId,
			HttpServletRequest request,
			HttpServletResponse response) {

		Place place = placeDao.get(placeId);
		if (place != null) {
			String mainUri = request.getScheme() + "://" 
					+ request.getServerName()
					+ ":" + request.getServerPort()
					+ request.getContextPath() 
					+ "/place/" + String.valueOf(placeId);
			place.setMainUri(mainUri);
			return new ModelAndView("place/get", "place", place);
		}
		
		response.setStatus(404);
		return null;

	}

}
