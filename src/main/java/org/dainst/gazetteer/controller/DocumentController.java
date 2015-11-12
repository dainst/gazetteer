package org.dainst.gazetteer.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceDeserializer;
import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.domain.ValidationResult;
import org.dainst.gazetteer.helpers.AncestorsHelper;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.LanguagesHelper;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.dainst.gazetteer.helpers.PolygonValidator;
import org.dainst.gazetteer.helpers.ProtectLocationsService;
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
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private RecordGroupRepository groupDao;
	
	@Autowired
	private JsonPlaceDeserializer jsonPlaceDeserializer;
	
	@Autowired
	private JsonPlaceSerializer jsonPlaceSerializer;
	
	@Autowired
	private ProtectLocationsService protectLocationsService;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Autowired
	private LanguagesHelper langHelper;
	
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
			@RequestParam(required=false) List<String> add,
			@RequestParam(required=false) boolean pretty,
			@RequestHeader(value="User-Agent", required=false) String userAgent,
			@RequestHeader(value="Accept", required=false) String accept,
			HttpServletRequest request,
			HttpServletResponse response) {
		
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
		if (userAgent != null && accept != null) {
			logger.debug("User-Agent: {}", userAgent);
			logger.debug("Accept: {}", accept);
			if ( (accept.contains("text/html") && suffix.isEmpty()) || ".html".equals(suffix) && !userAgent.contains("bot")) {
				RedirectView redirectView = new RedirectView(baseUri + "app/#!/show/" + placeId, true, true);
				redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
				logger.debug("Redirecting to app ...");
				return new ModelAndView(redirectView);
			}
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
			
			logger.debug("findByParent: {}", System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			
			List<Place> relatedPlaces = placeDao.findByIdIn(place.getRelatedPlaces());
			
			logger.debug("findByIdIn: {}", System.currentTimeMillis() - time);
			time = System.currentTimeMillis();
			
			User user = null;
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof User)
				user = (User) principal;
			protectLocationsService.protectLocations(user, place);
			
			List<Place> parents = new ArrayList<Place>();
			if (add != null && add.contains("parents")) {
				createParentsList(place, parents);
				
				for (Place parent : parents) {
					protectLocationsService.protectLocations(user, parent);
				}
			}
			
			PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
			
			boolean readAccess = placeAccessService.checkPlaceAccess(place, false);
			if (!readAccess)
				response.setStatus(403);
			
			jsonPlaceSerializer.setBaseUri(baseUri);
			jsonPlaceSerializer.setPretty(pretty);
			jsonPlaceSerializer.setIncludeAccessInfo(add != null && add.contains("access"));
			jsonPlaceSerializer.setIncludeChangeHistory(add != null && add.contains("history"));
			
			mav = new ModelAndView("place/get");
			if (layout != null) {
				mav.setViewName("place/"+layout);
			}
			mav.addObject("place", place);
			mav.addObject("relatedPlaces", relatedPlaces);
			if (parents.size() > 0) mav.addObject("parents", parents);
			mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
			mav.addObject("readAccess", readAccess);
			mav.addObject("editAccess", placeAccessService.checkPlaceAccess(place, true));
			mav.addObject("language", locale.getISO3Language());
			mav.addObject("limit", limit);
			mav.addObject("offset", offset);
			mav.addObject("view", view);
			mav.addObject("q", q);
			mav.addObject("nativePlaceName", place.getNameMap().get(locale.getISO3Language()));
			mav.addObject("googleMapsApiKey", googleMapsApiKey);
			mav.addObject("languages", langHelper.getLocalizedLanguages(locale));
			mav.addObject("langHelper", langHelper);
		}
		
		return mav;

	}
	
	@RequestMapping(value="/doc", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView createPlace(@RequestBody Place place,
			HttpServletResponse response) throws Exception {
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
		
		boolean accessGranted = placeAccessService.checkPlaceAccess(place, true);
		if (!accessGranted) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult();
			result.setSuccess(false);
			result.setMessage("accessDeniedError");
			response.setStatus(403);
			mav.addObject("result", result);
			return mav;
		}
		
		if (place.getPrefLocation() != null && place.getPrefLocation().getShape() != null && place.getPrefLocation().getShape().getCoordinates() != null) {
			PolygonValidator validator = new PolygonValidator();
			List<String> validationErrors = validator.validate(place.getPrefLocation().getShape());
			if (validationErrors.size() > 0) {
				ModelAndView mav = new ModelAndView("place/validation");
				ValidationResult result = new ValidationResult();
				result.setSuccess(false);
				String message = "polygonValidationError: ";
				for (String validationError : validationErrors) {
					message += "\n";
					message += validationError;
				}
				result.setMessage(message);
				response.setStatus(422);
				mav.addObject("result", result);
				return mav;
			}
		}
		
		place.setId(idGenerator.generate(place));
		updateRelatedPlaces(place, null);
		place.setLastChangeDate(new Date());
		
		Place existingPlace = placeDao.findOne(place.getId());
		
		if (existingPlace == null)
			place = placeDao.save(place);
		else
			throw new IllegalStateException("Could not create place! Generated ID already exists: " + place.getId());
		
		changeRecordDao.save(createChangeRecord(place, "create"));
		
		logger.debug("created place {}", place);
		
		increaseChildrenCount(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", place);
		mav.addObject("baseUri", baseUri);
		mav.addObject("readAccess", accessGranted);
		mav.addObject("editAccess", accessGranted);
		return mav;
		
	}
	
	@RequestMapping(value="/duplicate", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView duplicatePlace(@RequestBody Place place,
			HttpServletResponse response) throws Exception {
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
		
		boolean accessGranted = placeAccessService.checkPlaceAccess(place, true);
		if (!accessGranted) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult();
			result.setSuccess(false);
			result.setMessage("accessDeniedError");
			response.setStatus(403);
			mav.addObject("result", result);
			return mav;
		}
		
		place.setId(idGenerator.generate(place));
		place.setLastChangeDate(new Date());
		
		Place existingPlace = placeDao.findOne(place.getId());
		
		if (existingPlace == null)
			place = placeDao.save(place);
		else
			throw new IllegalStateException("Could not create place! Generated ID already exists: " + place.getId());
		
		changeRecordDao.save(createChangeRecord(place, "duplicate"));
		
		logger.debug("duplicated place {}", place);
		
		increaseChildrenCount(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", place);
		mav.addObject("baseUri", baseUri);
		mav.addObject("readAccess", accessGranted);
		mav.addObject("editAccess", accessGranted);
		return mav;
		
	}

	@RequestMapping(value="/doc/{placeId}", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView updateOrCreatePlace(@RequestBody Place place, 
			@PathVariable String placeId,
			HttpServletResponse response) throws Exception {

		Place placeToCheck = place;
		while (placeToCheck.getParent() != null && !placeToCheck.getParent().equals("")) {
			placeToCheck = placeDao.findOne(placeToCheck.getParent());
			
			if (placeToCheck.getId().equals(place.getId())) {
				ModelAndView mav = new ModelAndView("place/validation");
				ValidationResult result = new ValidationResult();
				result.setSuccess(false);
				result.setMessage("parentError");
				response.setStatus(422);
				mav.addObject("result", result);
				return mav;
			}
		}
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
		
		Place originalPlace = placeDao.findOne(place.getId());
		
		if (!placeAccessService.checkPlaceAccess(originalPlace, true)) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult();
			result.setSuccess(false);
			result.setMessage("accessDeniedError");
			response.setStatus(403);
			mav.addObject("result", result);
			return mav;
		}
		
		if (place.getPrefLocation() != null && place.getPrefLocation().getShape() != null && place.getPrefLocation().getShape().getCoordinates() != null) {
			PolygonValidator validator = new PolygonValidator();
			List<String> validationErrors = validator.validate(place.getPrefLocation().getShape());
			if (validationErrors.size() > 0) {
				ModelAndView mav = new ModelAndView("place/validation");
				ValidationResult result = new ValidationResult();
				result.setSuccess(false);
				String message = "polygonValidationError: ";
				for (String validationError : validationErrors) {
					message += "\n";
					message += validationError;
				}
				result.setMessage(message);
				response.setStatus(422);
				mav.addObject("result", result);
				return mav;
			}
		}
		
		if (place.getParent() != null && !place.getParent().equals(originalPlace.getParent())) {
			AncestorsHelper helper = new AncestorsHelper(placeDao);
			place.setAncestors(helper.findAncestorIds(place));
			if (place.getChildren() < 10000)
				helper.updateAncestors(place);
		}

		place.setLastChangeDate(new Date());
		
		if (placeDao.exists(place.getId()))
			changeRecordDao.save(createChangeRecord(place, "edit"));
		else
			changeRecordDao.save(createChangeRecord(place, "create"));
		
		place.setId(placeId);
		updateRelatedPlaces(place, originalPlace);
		place = placeDao.save(place);
		
		logger.debug("saved place {}", place);
		
		increaseChildrenCount(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
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
		
		List<Place> children = placeDao.findByParent(placeId);
		List<Place> relatedPlaces = placeDao.findByRelatedPlaces(placeId);
		Place place = placeDao.findOne(placeId);
		PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
		
		if (children.size() > 0 || relatedPlaces.size() > 0 || !placeAccessService.checkPlaceAccess(place, true)) {
			if (children.size() > 0)
				logger.debug("cannot delete place " + placeId + ": has " + children.size() + " children!");
			if (relatedPlaces.size() > 0)
				logger.debug("cannot delete place " + placeId + ": has " + relatedPlaces.size() + " related places!");
			if (!placeAccessService.checkPlaceAccess(place, true))
				logger.debug("cannot delete place " + placeId + ": no place access!");
			response.setStatus(409);
		} else {			
			place.setDeleted(true);
			place.setLastChangeDate(new Date());
			placeDao.save(place);
		
			changeRecordDao.save(createChangeRecord(place, "delete"));
			
			logger.debug("successfully deleted place " + placeId);
		
			response.setStatus(204);
		}
		
	}
	
	// add count for children (for scoring)
	private void increaseChildrenCount(Place place) {
		if (place.getParent() == null) return;
		Place parent = placeDao.findOne(place.getParent());
		while (parent != null) {
			parent.setChildren(parent.getChildren()+1);
			placeDao.save(parent);				
			logger.debug("updated children of {} count: {}", parent.getId(), parent.getChildren());
			if (parent.getParent() != null) {
				parent = placeDao.findOne(parent.getParent());
			} else {
				parent = null;
			}
		}
		
	}
	
	private void updateRelatedPlaces(Place place, Place originalPlace) {

		Set<String> currentRelatedPlaces = new HashSet<String>(); 
		if (originalPlace != null)
			currentRelatedPlaces = originalPlace.getRelatedPlaces();
		
		for (String relatedPlaceId : place.getRelatedPlaces()) {
			Place relatedPlace = placeDao.findOne(relatedPlaceId.replace(baseUri + "place/", ""));
			relatedPlace.addRelatedPlace(place.getId());
			placeDao.save(relatedPlace);
		}
			
		for (String currentRelatedPlaceId : currentRelatedPlaces) {
			if (currentRelatedPlaceId != null && !"null".equals(currentRelatedPlaceId)
					&& !place.getRelatedPlaces().contains(currentRelatedPlaceId)) {
				Place currentRelatedPlace = placeDao.findOne(currentRelatedPlaceId.replace(baseUri + "place/", ""));
				currentRelatedPlace.getRelatedPlaces().remove(place.getId());
				placeDao.save(currentRelatedPlace);
			}
		}	
	}

	private PlaceChangeRecord createChangeRecord(Place place, String changeType) {
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		PlaceChangeRecord changeRecord = new PlaceChangeRecord();
		changeRecord.setUserId(user.getId());
		changeRecord.setPlaceId(place.getId());
		changeRecord.setChangeType(changeType);
		changeRecord.setChangeDate(place.getLastChangeDate());
		
		return changeRecord;
	}
	
	private void createParentsList(Place place, List<Place> parents) {
		if (place.getParent() != null && !place.getParent().isEmpty()) {
			Place parent = placeDao.findOne(place.getParent());
			if (parent != null) {
				parents.add(parent);
				createParentsList(parent, parents);
			}
		}
	}	
}
