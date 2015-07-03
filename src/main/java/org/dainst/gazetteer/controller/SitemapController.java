package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SitemapController {

	//private static final Logger LOGGER = LoggerFactory.getLogger(SitemapController.class);
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Value("${baseUri}")
	private String baseUri;
	
	private static final int SITEMAP_SIZE = 1000;
	
	@RequestMapping(value="/sitemap_index.xml", method=RequestMethod.GET)
	public ModelAndView sitemapIndex() {
		ModelAndView mav = new ModelAndView("sitemap/index");
		long count = placeDao.count();
		mav.addObject("baseUri", baseUri);
		mav.addObject("no", count / SITEMAP_SIZE + 1);
		return mav;
	}
	
	@RequestMapping(value="/sitemap{no}.xml", method=RequestMethod.GET)
	public ModelAndView sitemap(@PathVariable int no) {
		ModelAndView mav = new ModelAndView("sitemap/sitemap");
		Page<Place> places = placeDao.findAll(new PageRequest(no-1, SITEMAP_SIZE));
		
		Map<String, Boolean> accessMap = new HashMap<String, Boolean>();
		
		for (Place place : places) {
			accessMap.put(place.getId(), checkPlaceAccess(place));
		}
		
		mav.addObject("no", no);
		mav.addObject("baseUri", baseUri);
		mav.addObject("places", places.getContent());
		mav.addObject("accessMap", accessMap);
		return mav;
	}
	
	private boolean checkPlaceAccess(Place place) {
		User user = null;
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		if (place.getRecordGroupId() != null && !place.getRecordGroupId().isEmpty() && 
				(user == null || !user.getRecordGroupIds().contains(place.getRecordGroupId())))
			return false;
		else
			return true;
	}
}
