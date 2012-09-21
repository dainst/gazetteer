package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.List;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.search.ElasticSearchPlaceQuery;
import org.dainst.gazetteer.search.ElasticSearchServer;
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
	
	@Autowired
	private ElasticSearchServer elasticSearchServer;
	
	@Autowired
	private JsonPlaceDeserializer jsonPlaceDeserializer;

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
	
	@RequestMapping(value="/widget/pick.js")
	public ModelAndView pickPlace(
			@RequestParam String callback,
			@RequestParam(required=false) String name,
			@RequestParam(required=false) String id,
			@RequestParam(required=false, value="class") String cssClass,
			@RequestParam(required=false) String value,
			@RequestParam(defaultValue="false") boolean disabled) {
		
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
			@RequestParam String callback,
			@RequestParam(defaultValue="*") String q,
			@RequestParam(defaultValue="10") int limit,
			@RequestParam(defaultValue="0") int offset) {
		
		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(elasticSearchServer.getClient(), jsonPlaceDeserializer);
		if (q != null) {
			query.fuzzyLikeThisSearch(q, "names.title");
		} else {
			query.listAll();
		}
		query.limit(limit);
		query.offset(offset);
		
		List<Place> places = query.execute();
		
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
