package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.dainst.gazetteer.dao.PlaceDao;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.ValidationResult;
import org.dainst.gazetteer.search.ElasticSearchPlaceQuery;
import org.dainst.gazetteer.search.ElasticSearchServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;


@Controller
public class PlaceController {

	private static final Logger logger = LoggerFactory.getLogger(PlaceController.class);
	
	@Autowired
	private PlaceDao placeDao;
	
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
	
	@RequestMapping(value="/place", method=RequestMethod.GET)
	public ModelAndView listPlaces(@RequestParam(defaultValue="10") int limit,
			@RequestParam(defaultValue="0") int offset,
			@RequestParam(required=false) String q,
			@RequestParam(required=false) String fuzzy,
			@RequestParam(required=false, defaultValue="map,table") String view,
			@RequestParam(required=false) String callback,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		
		logger.debug("Searching places with query: " + q + " and limit " + limit + ", offset " + offset);
		
		ElasticSearchPlaceQuery query = new ElasticSearchPlaceQuery(elasticSearchServer.getClient());
		if (q != null) {
			if ("true".equals(fuzzy)) query.fuzzySearch(q);
			else query.metaSearch(q);
		} else {
			query.listAll();
		}
		query.limit(limit);
		query.offset(offset);
		
		// get ids from elastic search
		int[] result = query.execute();
		
		logger.debug("Querying index returned: " + result.length + " places");
		
		// get places for the result ids from db
		List<Place> places = new ArrayList<Place>();
		for (int i = 0; i < result.length; i++) {
			places.add(placeDao.get(result[i]));
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
	
	
	// REST-Interface for single places

	@RequestMapping(value="/place/{placeId}", method=RequestMethod.GET)
	public ModelAndView getPlace(@PathVariable long placeId,
			@RequestParam(required=false) String layout,
			@RequestParam(defaultValue="10") int limit,
			@RequestParam(defaultValue="0") int offset,
			@RequestParam(required=false) String q,
			@RequestParam(required=false) String fuzzy,
			@RequestParam(required=false, defaultValue="map,table") String view,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();

		Place place = placeDao.get(placeId);
		if (place != null) {			
			ModelAndView mav = new ModelAndView("place/get");
			if (layout != null) {
				mav.setViewName("place/"+layout);
			}
			mav.addObject("place", place);
			mav.addObject("baseUri", baseUri);
			mav.addObject("language", locale.getISO3Language());
			mav.addObject("limit", limit);
			mav.addObject("offset", offset);
			mav.addObject("view", view);
			mav.addObject("q", q);
			mav.addObject("nativePlaceName", place.getNameMap().get(locale.getISO3Language()));
			logger.debug(locale.getISO3Language());
			mav.addObject("googleMapsApiKey", googleMapsApiKey);
			mav.addObject("languages", getLocalizedLanguages(locale));
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
	public ModelAndView updateOrCreatePlace(@RequestBody Place place, 
			@PathVariable long placeId,
			HttpServletResponse response) {
		
		place.setId(placeId);
		place = placeDao.save(place);
		
		response.setStatus(201);
		response.setHeader("location", baseUri + "place/" + place.getId());
		
		ModelAndView mav = new ModelAndView("place/validation");
		mav.addObject("result", new ValidationResult());	
		
		return mav;
		
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ModelAndView handleValidationException(HttpMessageNotReadableException e,
			HttpServletResponse response) {
		
		ValidationResult result = new ValidationResult();
		result.setSuccess(false);
		result.setMessage(e.getCause().getMessage());
		
		response.setStatus(400);
		
		ModelAndView mav = new ModelAndView("place/validation");
		mav.addObject("result", result);
		
		return mav;
		
	}
	
	@RequestMapping(value="/place/{placeId}", method=RequestMethod.DELETE)
	public void deletePlace(@PathVariable long placeId,
			HttpServletResponse response) {
		
		if(placeDao.setDeleted(placeId) != 0) {
			response.setStatus(204);
		} else {
			response.setStatus(404);
		}
		
	}
	
	private Map<String,String> getLocalizedLanguages(Locale locale) {
		HashMap<String, String> localizedLanguages = new HashMap<String,String>();
		for (String language : languages) {
			localizedLanguages.put(language, messageSource.getMessage("languages."+language, null, locale));
		}
		return localizedLanguages;
	}

}
