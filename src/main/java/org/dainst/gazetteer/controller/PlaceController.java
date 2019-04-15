package org.dainst.gazetteer.controller;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class PlaceController {

	private static final Logger logger = LoggerFactory.getLogger(PlaceController.class);
	
	@Resource(name="mediaTypes")
	Map<String,String> mediaTypes;
	
	@Autowired
	PlaceRepository placeRepository;

	@RequestMapping(value="/place/{id}", method=RequestMethod.GET)
	public View getPlace(@PathVariable String id,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RedirectView view;
		
		// 301 redirect for replaced places
		Place place = placeRepository.findById(id).orElse(null);
		if (place == null) {
			
			throw new ResourceNotFoundException();
			
		} else if (place.getReplacedBy() != null && !place.getReplacedBy().isEmpty()) {
			
			view = new RedirectView(place.getReplacedBy(), false, true);
			view.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
			
		// 303 redirect to document describing the place
		} else {
		
			String acceptHeader = request.getHeader("Accept");
			String suffix = "html";
			if (acceptHeader != null) for (Entry<String, String> entry : mediaTypes.entrySet()) {
				String uri = request.getRequestURI();
				if (uri.lastIndexOf(".") > 0)
					suffix = uri.substring(uri.lastIndexOf(".")+1);
				if (acceptHeader.contains(entry.getValue()))
					suffix = entry.getKey();
				logger.debug("suffix: {}", suffix);
			}
			
			logger.debug("suffix: {}", suffix);
			
			view = new RedirectView("../doc/" + id + "." + suffix, false, true);
			view.setStatusCode(HttpStatus.SEE_OTHER);
			
		}
		
		return view;
		
	}
	
}
