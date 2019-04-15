package org.dainst.gazetteer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.converter.ShapefileCreator;
import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
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
import org.dainst.gazetteer.search.ElasticSearchIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.GrantedAuthority;
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
	private PlaceChangeRecordRepository changeRecordDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private RecordGroupRepository groupDao;
	
	@Autowired
	private JsonPlaceSerializer jsonPlaceSerializer;
	
	@Autowired
	private ShapefileCreator shapefileCreator;
	
	@Autowired
	private ProtectLocationsService protectLocationsService;
	
	@Autowired
	private ElasticSearchIndexer indexer;
	
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
			@RequestParam(required=false) String add,
			@RequestParam(required=false) boolean pretty,
			@RequestParam(required=false) String replacing,
			@RequestHeader(value="User-Agent", required=false) String userAgent,
			@RequestHeader(value="Accept", required=false) String accept,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		Locale originalLocale = request.getLocale();

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
		
		Place place = placeDao.findById(placeId).orElse(null);
		
		if (place == null) {
			
			throw new ResourceNotFoundException();
			
		} else if (place.getReplacedBy() != null && !place.getReplacedBy().isEmpty()) {
			
			RedirectView redirectView = new RedirectView("/doc/" + place.getReplacedBy() + suffix, true, true);
			redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
			mav = new ModelAndView(redirectView);
			if (add != null && add.contains("replacing")) {
				mav.addObject("replacing", (replacing != null) ? replacing : placeId);
				mav.addObject("add", add);
			}
	
		// places that need to be reviewed should not be available to the public
		} else if (place.isNeedsReview()) {
			
			throw new ResourceNotFoundException();
			
		} else {
			
			List<Place> relatedPlaces = placeDao.findByIdIn(place.getRelatedPlaces());
					
			PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);			
			PlaceAccessService.AccessStatus accessStatus = placeAccessService.getAccessStatus(place);
			
			User user = null;
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			if (principal instanceof User)
				user = (User) principal;
			
			protectLocationsService.protectLocations(user, place, accessStatus);
			
			Map<String, PlaceAccessService.AccessStatus> parentAccessStatusMap = new HashMap<String, PlaceAccessService.AccessStatus>();
			
			List<Place> parents = new ArrayList<Place>();
			
			if (add != null && add.contains("parents")) {
				createParentsList(place, parents);
				
				for (Place parent : parents) {
					parentAccessStatusMap.put(parent.getId(), placeAccessService.getAccessStatus(parent));
					protectLocationsService.protectLocations(user, parent, placeAccessService.getAccessStatus(parent));
				}
			}
			
			if (!placeAccessService.hasReadAccess(place))
				response.setStatus(403);
						
			jsonPlaceSerializer.setBaseUri(baseUri);
			jsonPlaceSerializer.setPretty(pretty);
			jsonPlaceSerializer.setIncludeAccessInfo(add != null && add.contains("access"));
			jsonPlaceSerializer.setIncludeChangeHistory(add != null && add.contains("history"));
			if (add != null && add.contains("sort")) {
				jsonPlaceSerializer.setLocale(locale);
				jsonPlaceSerializer.setOriginalLocale(originalLocale);
			}
			else {
				jsonPlaceSerializer.setLocale(null);
				jsonPlaceSerializer.setOriginalLocale(null);
			}
			
			mav = new ModelAndView("place/get");
			if (layout != null) {
				mav.setViewName("place/"+layout);
			}
			mav.addObject("place", place);
			mav.addObject("replacing", replacing);
			mav.addObject("relatedPlaces", relatedPlaces);
			if (parents.size() > 0) mav.addObject("parents", parents);
			mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
			mav.addObject("accessStatus", accessStatus);
			mav.addObject("parentAccessStatusMap", parentAccessStatusMap);
			mav.addObject("language", locale.getISO3Language());
			mav.addObject("limit", limit);
			mav.addObject("offset", offset);
			mav.addObject("view", view);
			mav.addObject("q", q);
			mav.addObject("nativePlaceName", place.getNameMap().get(locale.getISO3Language()));
			mav.addObject("googleMapsApiKey", googleMapsApiKey);
			mav.addObject("languages", langHelper.getLocalizedLanguages(locale));
			mav.addObject("langHelper", langHelper);
			mav.addObject("baseUri", baseUri);
		}
		
		return mav;

	}
	
	@RequestMapping(value="/doc/shapefile/{placeId}", method=RequestMethod.GET)
	public void getShapefile(@PathVariable String placeId,
			HttpServletRequest request,
			HttpServletResponse response) {
		
		Place place = placeDao.findById(placeId).orElse(null);
		
		if (place == null || place.isDeleted() || place.isNeedsReview()) {
			throw new ResourceNotFoundException();
		}
				
		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);
		PlaceAccessService.AccessStatus accessStatus = placeAccessService.getAccessStatus(place);
		
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		protectLocationsService.protectLocations(user, place, accessStatus);
	
		if (!placeAccessService.hasReadAccess(place)) {
			response.setStatus(403);
			return;
		}
		
		File file = null;
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH-mm");

		try {
			file = shapefileCreator.createShapefile("iDAIgazetteer_" + "id" + place.getId() + "_" + dateFormat.format(new Date()), place.getId());
		} catch (Exception e) {
			throw new RuntimeException("Shapefile creation failed", e);
		}
		
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Shapefile could not be found", e);
		}
		
		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=" + file.getName()); 

		try {
			IOUtils.copy(inputStream, response.getOutputStream());
			response.flushBuffer();
	    } catch (IOException e) {
	    	throw new RuntimeException("Failed to copy zipped shapefile to output stream", e);
	    } finally {
	    	try {
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException("Failed to close input stream", e);
			}
	    }

		try {
			shapefileCreator.removeShapefileData(file);
		} catch (IOException e) {
			throw new RuntimeException("Failed to remove shapefile data", e);
		}
		
	}
	
	@RequestMapping(value="/doc", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView createPlace(@RequestBody Place place,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		RequestContext requestContext = new RequestContext(request);
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);
		
		if (!placeAccessService.hasEditAccess(place)) {
			logger.debug("No edit access");
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult(false, "accessDeniedError");
			response.setStatus(403);
			mav.addObject("result", result);
			return mav;
		}
		
		if (place.getPrefLocation() != null && place.getPrefLocation().getShape() != null && place.getPrefLocation().getShape().getCoordinates() != null) {
			PolygonValidator validator = new PolygonValidator();
			ValidationResult result = validator.validate(place.getPrefLocation().getShape());
			if (!result.isSuccess()) {
				ModelAndView mav = new ModelAndView("place/validation");
				response.setStatus(422);
				mav.addObject("result", result);
				return mav;
			}
		}
		
		place.setId(idGenerator.generate(place));
		updateRelatedPlaces(place, null);
		place.setLastChangeDate(new Date());
		
		Place existingPlace = placeDao.findById(place.getId()).orElse(null);
		
		if (existingPlace == null)
			place = placeDao.save(place);
		else
			throw new IllegalStateException("Could not create place! Generated ID already exists: " + place.getId());
		
		changeRecordDao.save(createChangeRecord(place, "create"));
		
		logger.debug("created place {}", place);
		
		increaseChildrenCount(place);
		indexer.index(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
		jsonPlaceSerializer.setBaseUri(baseUri);
		jsonPlaceSerializer.setPretty(false);
		jsonPlaceSerializer.setIncludeAccessInfo(false);
		jsonPlaceSerializer.setIncludeChangeHistory(false);
		jsonPlaceSerializer.setLocale(requestContext.getLocale());
		
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", place);
		mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
		mav.addObject("accessStatus", placeAccessService.getAccessStatus(place));
		return mav;
		
	}
	
	@RequestMapping(value="/duplicate", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView duplicatePlace(@RequestBody Place place,
			HttpServletResponse response) throws Exception {
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);
		
		if (!placeAccessService.hasEditAccess(place)) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult(false, "accessDeniedError");
			response.setStatus(403);
			mav.addObject("result", result);
			return mav;
		}
		
		place.setId(idGenerator.generate(place));
		place.setLastChangeDate(new Date());
		
		Place existingPlace = placeDao.findById(place.getId()).orElse(null);
		
		if (existingPlace == null)
			place = placeDao.save(place);
		else
			throw new IllegalStateException("Could not create place! Generated ID already exists: " + place.getId());
		
		changeRecordDao.save(createChangeRecord(place, "duplicate"));
		
		logger.debug("duplicated place {}", place);
		
		increaseChildrenCount(place);
		indexer.index(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", place);
		mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
		mav.addObject("accessStatus", placeAccessService.getAccessStatus(place));
		return mav;
		
	}

	@RequestMapping(value="/doc/{placeId}", method={RequestMethod.POST, RequestMethod.PUT})
	public ModelAndView updateOrCreatePlace(@RequestBody Place place, 
			@PathVariable String placeId,
			HttpServletResponse response) throws Exception {
		
		place.setId(placeId);
		
		Place originalPlace = placeDao.findById(place.getId()).orElse(null);
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);
		
		if (!placeAccessService.hasEditAccess(originalPlace)) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult(false, "accessDeniedError");
			response.setStatus(403);
			mav.addObject("result", result);
			return mav;
		}
		
		if (!hasValidParent(place)) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult(false, "parentError");
			response.setStatus(422);
			mav.addObject("result", result);
			return mav;
		}
		
		if (!hasValidRelatedPlaces(place)) {
			ModelAndView mav = new ModelAndView("place/validation");
			ValidationResult result = new ValidationResult(false, "relatedPlaceError");
			response.setStatus(422);
			mav.addObject("result", result);
			return mav;
		}
		
		if (place.getPrefLocation() != null && place.getPrefLocation().getShape() != null && place.getPrefLocation().getShape().getCoordinates() != null) {
			PolygonValidator validator = new PolygonValidator();
			ValidationResult result = validator.validate(place.getPrefLocation().getShape());
			if (!result.isSuccess()) {
				ModelAndView mav = new ModelAndView("place/validation");
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
		
		if (placeDao.existsById(place.getId()))
			changeRecordDao.save(createChangeRecord(place, "edit"));
		else
			changeRecordDao.save(createChangeRecord(place, "create"));
		
		place.setId(placeId);
		updateRelatedPlaces(place, originalPlace);
		place = placeDao.save(place);
		
		logger.debug("saved place {}", place);
		
		increaseChildrenCount(place);
		indexer.index(place);
		
		response.setStatus(201);
		response.setHeader("Location", baseUri + "place/" + place.getId());
		
		ModelAndView mav = new ModelAndView("place/validation");
		mav.addObject("result", new ValidationResult(true));
		
		return mav;
		
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ModelAndView handleValidationException(HttpMessageNotReadableException e,
			HttpServletResponse response) {
		
		ValidationResult result = new ValidationResult(false, e.getCause().getMessage(), "", "");
		
		response.setStatus(400);
		
		ModelAndView mav = new ModelAndView("place/validation");
		mav.addObject("result", result);
		
		return mav;
		
	}
	
	@RequestMapping(value="/doc/{placeId}", method=RequestMethod.DELETE)
	public void deletePlace(@PathVariable String placeId,
			HttpServletResponse response) {
		
		logger.debug("Deleting place " + placeId + "...");
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (user == null)
			logger.debug("No user logged in!");
		else {
			logger.debug("User is logged in!");
			for (GrantedAuthority authority : user.getAuthorities()) {
				logger.debug("User has authority: " + authority.getAuthority());
			}
		}
		
		List<Place> children = placeDao.findByParent(placeId);
		List<Place> relatedPlaces = placeDao.findByRelatedPlaces(placeId);
		Place place = placeDao.findById(placeId).orElse(null);
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupDao, groupRoleDao);

		if (children.size() > 0 || relatedPlaces.size() > 0 || !placeAccessService.hasEditAccess(place)) {
			if (children.size() > 0)
				logger.debug("cannot delete place " + placeId + ": has " + children.size() + " children!");
			if (relatedPlaces.size() > 0)
				logger.debug("cannot delete place " + placeId + ": has " + relatedPlaces.size() + " related places!");
			if (!placeAccessService.hasEditAccess(place))
				logger.debug("cannot delete place " + placeId + ": no place access!");
			response.setStatus(409);
		} else {			
			place.setDeleted(true);
			place.setLastChangeDate(new Date());
			
			placeDao.save(place);
			changeRecordDao.save(createChangeRecord(place, "delete"));
			indexer.index(place);
			
			logger.debug("successfully deleted place " + placeId);
		
			response.setStatus(204);
		}
		
	}
	
	// add count for children (for scoring)
	private void increaseChildrenCount(Place place) {
		if (place.getParent() == null) return;
		Place parent = placeDao.findById(place.getParent()).orElse(null);
		while (parent != null) {
			parent.setChildren(parent.getChildren()+1);
			placeDao.save(parent);				
			logger.debug("updated children of {} count: {}", parent.getId(), parent.getChildren());
			if (parent.getParent() != null) {
				parent = placeDao.findById(parent.getParent()).orElse(null);
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
			Place relatedPlace = placeDao.findById(relatedPlaceId.replace(baseUri + "place/", "")).orElse(null);
			relatedPlace.addRelatedPlace(place.getId());
			placeDao.save(relatedPlace);
			indexer.index(relatedPlace);
		}
			
		for (String currentRelatedPlaceId : currentRelatedPlaces) {
			if (currentRelatedPlaceId != null && !"null".equals(currentRelatedPlaceId)
					&& !place.getRelatedPlaces().contains(currentRelatedPlaceId)) {
				Place currentRelatedPlace = placeDao.findById(currentRelatedPlaceId.replace(baseUri + "place/", "")).orElse(null);
				currentRelatedPlace.getRelatedPlaces().remove(place.getId());
				placeDao.save(currentRelatedPlace);
				indexer.index(currentRelatedPlace);
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
			Place parent = placeDao.findById(place.getParent()).orElse(null);
			if (parent != null) {
				parents.add(parent);
				createParentsList(parent, parents);
			}
		}
	}
	
	private boolean hasValidParent(Place place) {
		
		Place placeToCheck = place;
		
		while (placeToCheck.getParent() != null && !placeToCheck.getParent().equals("")) {
			placeToCheck = placeDao.findById(placeToCheck.getParent()).orElse(null);
			
			if (placeToCheck.getId().equals(place.getId())) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean hasValidRelatedPlaces(Place place) {
		
		for (String relatedPlaceId : place.getRelatedPlaces()) {
			if (relatedPlaceId.equals(place.getId())) {
				return false;
			}
		}
		
		return true;
	}
}
