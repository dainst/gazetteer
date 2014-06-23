package org.dainst.gazetteer.controller;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.domain.ValidationResult;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.LocalizedLanguagesHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
	private UserRepository userDao;
	
	@Autowired
	private PlaceChangeRecordRepository changeRecordDao;
	
	@Autowired
	private JsonPlaceDeserializer jsonPlaceDeserializer;
	
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
			@RequestHeader("User-Agent") String userAgent,
			@RequestHeader("Accept") String accept,
			HttpServletRequest request) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();

		ModelAndView mav;
		
		String suffix = "";
		String uri = request.getRequestURI();
		logger.debug("uri: " + uri);
		if (uri.lastIndexOf(".") > 0) {
			suffix = uri.substring(uri.lastIndexOf("."));
			logger.debug("suffix: " + suffix);
		}
		
		// redirect browsers to app
		logger.debug("User-Agent: {}", userAgent);
		logger.debug("Accept: {}", accept);
		if ( (accept.contains("text/html") && suffix.isEmpty()) || ".html".equals(suffix) && !userAgent.contains("bot")) {
			RedirectView redirectView = new RedirectView(baseUri + "app/#!/show/" + placeId, true, true);
			redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
			logger.debug("Redirecting to app ...");
			return new ModelAndView(redirectView);
		}
		
		long time = System.currentTimeMillis();
		
		Place place = placeDao.findOne(placeId);
		
		logger.debug("findOne: {}", System.currentTimeMillis() - time);
		time = System.currentTimeMillis();
		
		if (place == null) {
			
			throw new ResourceNotFoundException();
			
		} else if (place.getReplacedBy() != null && !place.getReplacedBy().isEmpty()) {
			
			RedirectView redirectView = new RedirectView("/doc/" + place.getReplacedBy() + suffix, true, true);
			redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
			mav = new ModelAndView(redirectView);
		
		// places that need to be reviewed should not be available to the public
		} else if (place.isNeedsReview()) {
			
			throw new ResourceNotFoundException();
			
		} else {
			
			//List<Place> children = placeDao.findByParent(place.getId());
			
			logger.debug("findByParent: {}", System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			
			List<Place> relatedPlaces = placeDao.findByIdIn(place.getRelatedPlaces());
			
			logger.debug("findByIdIn: {}", System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			
			//Place parent = null;
			//if (place.getParent() != null) parent = placeDao.findOne(place.getParent());
			
			mav = new ModelAndView("place/get");
			if (layout != null) {
				mav.setViewName("place/"+layout);
			}
			mav.addObject("place", place);
			//mav.addObject("children", children);
			mav.addObject("relatedPlaces", relatedPlaces);
			//mav.addObject("parent", parent);
			mav.addObject("baseUri", baseUri);
			mav.addObject("language", locale.getISO3Language());
			mav.addObject("limit", limit);
			mav.addObject("offset", offset);
			mav.addObject("view", view);
			mav.addObject("q", q);
			mav.addObject("nativePlaceName", place.getNameMap().get(locale.getISO3Language()));
			mav.addObject("userDao", userDao);
			mav.addObject("changeRecordDao", changeRecordDao);
			mav.addObject("googleMapsApiKey", googleMapsApiKey);
			mav.addObject("languages", langHelper.getLocalizedLanguages(locale));			
		}
		
		return mav;

	}
	
	@RequestMapping(value="/doc", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView createPlace(@RequestBody Place place,
			HttpServletResponse response) {
		
		place.setId(idGenerator.generate(place));
		place = placeDao.save(place);
		
		changeRecordDao.save(createChangeRecord(place, "create"));
		
		logger.debug(place.getId());
		
		// add count for children (for scoring)
		while (place.getParent() != null) {
			place = placeDao.findOne(place.getParent());
			place.setChildren(place.getChildren()+1);
			placeDao.save(place);
			logger.debug("updated children count: {}", place.getChildren());
		}
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", place);
		mav.addObject("baseUri", baseUri);
		return mav;
		
	}
	
	@RequestMapping(value="/doc/{placeId}", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView updateOrCreatePlace(@RequestBody Place place, 
			@PathVariable String placeId,
			HttpServletResponse response) {
		
		if (placeDao.equals(place))
			changeRecordDao.save(createChangeRecord(place, "edit"));
		else
			changeRecordDao.save(createChangeRecord(place, "create"));
		
		place.setId(placeId);
		place = placeDao.save(place);
		
		
		
		logger.debug("saved place {}", place);
		
		// add count for children (for scoring)
		if (place.getParent() != null) {
			Place parent = placeDao.findOne(place.getParent());
			try {
				parent.setChildren(parent.getChildren()+1);
				placeDao.save(parent);				
				logger.debug("updated children count: {}", parent.getChildren());
			} catch (NullPointerException e) {
				logger.warn("Could not find parent {} for {}", place.getParent(), place);
			}
		}
		
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
		
		changeRecordDao.save(createChangeRecord(place, "delete"));
		
		List<Place> children = placeDao.findByParent(placeId);
		for (Place child : children) {
			child.setParent(null);
			placeDao.save(child);
		}
		
		List<Place> relatedPlaces = placeDao.findByRelatedPlaces(placeId);
		for (Place relatedPlace : relatedPlaces) {
			relatedPlace.getRelatedPlaces().remove(placeId);
			placeDao.save(relatedPlace);
		}
		
		response.setStatus(204);
		
	}

	private PlaceChangeRecord createChangeRecord(Place place, String changeType) {
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		PlaceChangeRecord changeRecord = new PlaceChangeRecord();
		changeRecord.setUserId(user.getId());
		changeRecord.setPlaceId(place.getId());
		changeRecord.setChangeType(changeType);
		changeRecord.setChangeDate(new Date());
		
		return changeRecord;
	}
}
