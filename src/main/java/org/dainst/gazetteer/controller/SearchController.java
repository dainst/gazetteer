package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.search.ElasticSearchPlaceQuery;
import org.dainst.gazetteer.search.ElasticSearchServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class SearchController {

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private JsonPlaceDeserializer jsonPlaceDeserializer;
	
	@Autowired
	private ElasticSearchServer elasticSearchServer;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Value("${languages}")
	private String[] languages;
	
	@Value("${googleMapsApiKey}")
	private String googleMapsApiKey;
	
	@Autowired
	MessageSource messageSource;
	
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public ModelAndView listPlaces(@RequestParam(defaultValue="10") int limit,
			@RequestParam(defaultValue="0") int offset,
			@RequestParam(required=false) String q,
			@RequestParam(required=false) String sort,
			@RequestParam(defaultValue="asc") String order,
			@RequestParam(required=false) String type,
			@RequestParam(required=false, defaultValue="map,table") String view,
			@RequestParam(required=false) String callback,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		
		logger.debug("Searching places with query: " + q + " and limit " + limit + ", offset " + offset);
		
		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(elasticSearchServer.getClient());
		if (q != null) {
			if ("fuzzy".equals(type)) query.fuzzySearch(q);
			else if ("prefix".equals(type)) query.prefixSearch(q);
			else if ("queryString".equals(type)) query.queryStringSearch(q);
			else query.metaSearch(q);
		} else {
			query.listAll();
		}
		query.addBoostForChildren();
		query.limit(limit);
		query.offset(offset);
		if (sort != null && !sort.isEmpty()) {
			query.addSort(sort, order);
		}
		query.addFilter("deleted:false");
		
		// get ids from elastic search
		String[] result = query.execute();
		
		logger.debug("Querying index returned: " + result.length + " places");
		
		// get places for the result ids from db
		List<Place> places = new ArrayList<Place>();
		for (int i = 0; i < result.length; i++) {
			places.add(placeDao.findOne(result[i]));
		}
		
		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		mav.addObject("baseUri", baseUri);
		mav.addObject("language", locale.getISO3Language());
		mav.addObject("limit", limit);
		mav.addObject("offset", offset);
		mav.addObject("hits", query.getHits());
		mav.addObject("view", view);
		mav.addObject("q", q);
		mav.addObject("googleMapsApiKey", googleMapsApiKey);
		mav.addObject("callback", callback);
		
		return mav;
		
	}
	
	@RequestMapping(value="/geoSearch", method=RequestMethod.GET)
	public ModelAndView geoList(@RequestParam(defaultValue="10") int limit,
			@RequestParam(defaultValue="0") int offset,
			@RequestParam double lat,
			@RequestParam double lon,
			@RequestParam(defaultValue="50") int distance,
			@RequestParam(required=false) String filter,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		
		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(elasticSearchServer.getClient());
		query.geoDistanceSearch(lon, lat, distance);
		query.addGeoDistanceSort(lon, lat);
		query.limit(limit);
		query.offset(offset);
		query.addFilter("deleted:false");
		
		if (filter != null) {
			query.addFilter(filter);
		}
		
		// get ids from elastic search
		String[] result = query.execute();
		
		logger.debug("Querying index returned: " + result.length + " places");
		
		// get places for the result ids from db
		List<Place> places = new ArrayList<Place>();
		for (int i = 0; i < result.length; i++) {
			places.add(placeDao.findOne(result[i]));
		}
		
		ModelAndView mav = new ModelAndView("place/list");
		mav.addObject("places", places);
		mav.addObject("baseUri", baseUri);
		mav.addObject("language", locale.getISO3Language());
		mav.addObject("limit", limit);
		mav.addObject("offset", offset);
		mav.addObject("hits", query.getHits());
		mav.addObject("googleMapsApiKey", googleMapsApiKey);
		
		return mav;
		
	}
	
}
