package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.List;

import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.search.ElasticSearchClientProvider;
import org.dainst.gazetteer.search.ElasticSearchPlaceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WidgetController {
	
	private static final Logger logger = LoggerFactory.getLogger(WidgetController.class);
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private ElasticSearchClientProvider clientProvider;

	@RequestMapping(value="/widget/lib.js")
	public ModelAndView getLibJs() {
		
		ModelAndView mav = new ModelAndView("widget/lib");
		mav.addObject("baseUri", baseUri);
		
		return mav;
		
	}
	
	@RequestMapping(value="/widget/show.js")
	public ModelAndView showPlace(
			@RequestParam(name="callback") String callback,
			@RequestParam(required=false, value="id") String[] ids,
			@RequestParam(name="mapHeight", defaultValue="150") int mapHeight,
			@RequestParam(name="showInfo", defaultValue="false") boolean showInfo) {
		
		ArrayList<Place> places = new ArrayList<Place>();
		for (int i = 0; i < ids.length; i++) {
			Place place = placeDao.findById(ids[i]).orElse(null);
			if(place != null) places.add(place);
		}
		
		logger.debug("places:", places);
		
		ModelAndView mav = new ModelAndView("widget/show");
		mav.addObject("places", places);
		mav.addObject("callback", callback);
		mav.addObject("baseUri", baseUri);
		mav.addObject("mapHeight", mapHeight);
		mav.addObject("showInfo", showInfo);
		
		return mav;
		
	}
	
	@RequestMapping(value="/widget/pick.js")
	public ModelAndView pickPlace(
			@RequestParam(name="callback") String callback,
			@RequestParam(name="name", required=false) String name,
			@RequestParam(name="id", required=false) String id,
			@RequestParam(required=false, value="class") String cssClass,
			@RequestParam(name="value", required=false) String value,
			@RequestParam(name="disabled", defaultValue="false") boolean disabled) {
		
		ModelAndView mav = new ModelAndView("widget/pick");		
		mav.addObject("callback", callback);
		mav.addObject("name", name);
		mav.addObject("id", id);
		mav.addObject("cssClass", cssClass);
		mav.addObject("value", value);
		mav.addObject("disabled", disabled);
		return mav;
		
	}
	
	@RequestMapping(value="/widget/search.js")
	public ModelAndView searchPlaces(
			@RequestParam(name="callback") String callback,
			@RequestParam(name="q", defaultValue="*") String q,
			@RequestParam(name="limit", defaultValue="10") int limit,
			@RequestParam(name="offset", defaultValue="0") int offset
    ) {
		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(clientProvider.getClient());
		if (q != null) {
			query.queryStringSearch(q);
		} else {
			query.listAll();
		}
		query.limit(limit);
		query.offset(offset);
		
		// get ids from elastic search
		String[] result = query.execute();
		
		// get places for the result ids from db
		List<Place> places = new ArrayList<Place>();
		for (int i = 0; i < result.length; i++) {
			places.add(placeDao.findById(result[i]).orElse(null));
		}
		
		ModelAndView mav = new ModelAndView("widget/search");
		mav.addObject("places", places);
		mav.addObject("baseUri", baseUri);
		mav.addObject("limit", limit);
		mav.addObject("offset", offset);
		mav.addObject("q", q);
		mav.addObject("callback", callback);
		
		return mav;
		
	}
	
}
