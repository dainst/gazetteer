package org.dainst.gazetteer.controller;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.AncestorsHelper;
import org.dainst.gazetteer.helpers.IdGenerator;
import org.dainst.gazetteer.helpers.Merger;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContext;

@Controller
public class MergeController {

	private static final Logger logger = LoggerFactory.getLogger(MergeController.class);
	
	@Autowired
	private PlaceRepository placeDao;
	
	@Autowired
	private PlaceChangeRecordRepository changeRecordDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private Merger merger;
	
	@Autowired
	private IdGenerator idGenerator;
	
	@Autowired
	private JsonPlaceSerializer jsonPlaceSerializer;
	
	@Value("${baseUri}")
	private String baseUri;

	@RequestMapping(value="/merge/{id1}/{id2}", method=RequestMethod.POST)
	public ModelAndView getPlace(@PathVariable String id1,
			@PathVariable String id2,
			HttpServletRequest request,
			HttpServletResponse response) {
				
		Place place1 = placeDao.findOne(id1);
		Place place2 = placeDao.findOne(id2);
		
		PlaceAccessService placeAccessService = new PlaceAccessService(groupRoleDao);
		
		if (!placeAccessService.checkPlaceAccess(place1, true) || !placeAccessService.checkPlaceAccess(place2, true))
			throw new IllegalStateException("Places may not be merged, as the user doesn't have the permission to edit both places.");
		
		if (!(place1.getRecordGroupId() == null && place2.getRecordGroupId() == null) && (place1.getRecordGroupId() != null && !place1.getRecordGroupId().equals(place2.getRecordGroupId())))
			throw new IllegalStateException("Places may not be merged, as they belong to different record groups.");
		
		// merge places
		merger.merge(place1, place2);

		// update ancestor ids
		AncestorsHelper helper = new AncestorsHelper(placeDao);
		place1.setAncestors(helper.findAncestorIds(place1));
		
		Set<Place> updatedPlaces = new HashSet<Place>();
		try {
			// update IDs in related places
			for (String relatedPlaceId : place1.getRelatedPlaces()) {
				Place relatedPlace = placeDao.findOne(relatedPlaceId);
				if (relatedPlace != null && relatedPlace.getRelatedPlaces() != null) {
					relatedPlace.getRelatedPlaces().remove(id1);
					relatedPlace.getRelatedPlaces().remove(id2);
					relatedPlace.getRelatedPlaces().add(place1.getId());
					updatedPlaces.add(relatedPlace);
				}
			}
		
			// update IDs in children of place 2
			List<Place> children = placeDao.findByParent(id2);
			for (Place child : children) {
				child.setParent(place1.getId());
				updatedPlaces.add(child);
			}
		} catch (Exception e) {		
			throw new RuntimeException("Could not merge places: Failed to update related places / children.", e);
		}
		
		for (Place place : updatedPlaces) {
			placeDao.save(place);
		}
		
		// save merged place
		placeDao.save(place1);
		
		if (place1.getChildren() + place2.getChildren() < 10000)
			helper.updateAncestors(place1);

		// Delete place 2
		place2.setReplacedBy(place1.getId());
		place2.setDeleted(true);
		placeDao.save(place2);

		changeRecordDao.save(createChangeRecord(place2, "replace", place1.getId()));
		changeRecordDao.save(createChangeRecord(place1, "merge", place2.getId()));
	
		logger.debug("finished merging " + place1.getId() + " and " + place2.getId() + " to " + place1.getId());
		
		response.setStatus(201);
		response.setHeader("location", baseUri + "place/" + place1.getId());
		
		RequestContext requestContext = new RequestContext(request);
		Locale locale = requestContext.getLocale();
		
		jsonPlaceSerializer.setBaseUri(baseUri);
		jsonPlaceSerializer.setPretty(false);
		jsonPlaceSerializer.setIncludeAccessInfo(true);
		jsonPlaceSerializer.setIncludeChangeHistory(true);
		jsonPlaceSerializer.setLocale(locale);
		
		ModelAndView mav = new ModelAndView("place/get");
		mav.addObject("place", place1);
		mav.addObject("baseUri", baseUri);
		mav.addObject("readAccess", true);
		mav.addObject("editAccess", true);
		mav.addObject("accessGranted", placeAccessService.checkPlaceAccess(place1));
		mav.addObject("jsonPlaceSerializer", jsonPlaceSerializer);
		return mav;
		
	}
	
	private PlaceChangeRecord createChangeRecord(Place place, String changeType, String additionalData) {
		
		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		
		PlaceChangeRecord changeRecord = new PlaceChangeRecord();
		changeRecord.setUserId(user.getId());
		changeRecord.setPlaceId(place.getId());
		changeRecord.setChangeType(changeType);
		changeRecord.setChangeDate(new Date());
		changeRecord.setAdditionalData(additionalData);
		
		return changeRecord;
	}
}
