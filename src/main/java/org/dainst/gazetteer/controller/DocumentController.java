package org.dainst.gazetteer.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.ValidationResult;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.LocalizedLanguagesHelper;
import org.dainst.gazetteer.search.ElasticSearchServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class DocumentController {

	private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private JsonPlaceDeserializer jsonPlaceDeserializer;
	
	@Autowired
	private ElasticSearchServer elasticSearchServer;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Autowired
	private LocalizedLanguagesHelper langHelper;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Value("${googleMapsApiKey}")
	private String googleMapsApiKey;

	@RequestMapping(value="/doc/{placeId}", method=RequestMethod.GET)
	public ModelAndView getPlace(@PathVariable String placeId,
			@RequestParam(required=false) String layout,
			@RequestParam(defaultValue="10") int limit,
			@RequestParam(defaultValue="0") int offset,
			@RequestParam(required=false) String q,
			@RequestParam(required=false) String fuzzy,
			@RequestParam(required=false, defaultValue="map,table") String view,
			HttpServletRequest request) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();

		ModelAndView mav;
		
		Place place = placeDao.findOne(placeId);
		if (place == null) {
			
			throw new ResourceNotFoundException();
			
		} else if (place.getReplacedBy() != null && !place.getReplacedBy().isEmpty()) {
			
			String suffix = "";
			String uri = request.getRequestURI();
			logger.debug("uri: " + uri);
			if (uri.lastIndexOf(".") > 0) {
				suffix = uri.substring(uri.lastIndexOf("."));
				logger.debug("suffix: " + suffix);
			}
			
			RedirectView redirectView = new RedirectView("/doc/" + place.getReplacedBy() + suffix, true, true);
			redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
			mav = new ModelAndView(redirectView);
			
		} else {
			
			List<Place> children = placeDao.findByIdIn(place.getChildren());
			List<Place> relatedPlaces = placeDao.findByIdIn(place.getRelatedPlaces());
			Place parent = null;
			if (place.getParent() != null) parent = placeDao.findOne(place.getParent());
			
			mav = new ModelAndView("place/get");
			if (layout != null) {
				mav.setViewName("place/"+layout);
			}
			mav.addObject("place", place);
			mav.addObject("children", children);
			mav.addObject("relatedPlaces", relatedPlaces);
			mav.addObject("parent", parent);
			mav.addObject("baseUri", baseUri);
			mav.addObject("language", locale.getISO3Language());
			mav.addObject("limit", limit);
			mav.addObject("offset", offset);
			mav.addObject("view", view);
			mav.addObject("q", q);
			mav.addObject("nativePlaceName", place.getNameMap().get(locale.getISO3Language()));
			mav.addObject("googleMapsApiKey", googleMapsApiKey);
			mav.addObject("languages", langHelper.getLocalizedLanguages(locale));
			
		}
		
		return mav;

	}
	
	@RequestMapping(value="/doc", method=RequestMethod.POST)
	public void createPlace(@RequestBody Place place,
			HttpServletResponse response) {
		
		place.setId(idGenerator.generate(place));		
		place = placeDao.save(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
	}
	
	@RequestMapping(value="/doc/{placeId}", method=RequestMethod.PUT)
	public ModelAndView updateOrCreatePlace(@RequestBody Place place, 
			@PathVariable String placeId,
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
	
	@RequestMapping(value="/doc/{placeId}", method=RequestMethod.DELETE)
	public void deletePlace(@PathVariable String placeId,
			HttpServletResponse response) {
		
		Place place = placeDao.findOne(placeId);
		place.setDeleted(true);
		placeDao.save(place);
		
		response.setStatus(204);
		
	}

}
