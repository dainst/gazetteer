package org.dainst.gazetteer.controller;

import java.util.Date;
import java.util.Locale;
import java.util.Set;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dainst.gazetteer.converter.JsonPlaceSerializer;
import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.User;
import org.dainst.gazetteer.helpers.AncestorsHelper;
import org.dainst.gazetteer.helpers.MailService;
import org.dainst.gazetteer.helpers.Merger;
import org.dainst.gazetteer.helpers.PlaceAccessService;
import org.dainst.gazetteer.search.ElasticSearchIndexer;
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
	private RecordGroupRepository recordGroupDao;
	
	@Autowired
	private GroupRoleRepository groupRoleDao;
	
	@Autowired
	private Merger merger;
	
	@Autowired
	private ElasticSearchIndexer indexer;
	
	@Autowired
	private JsonPlaceSerializer jsonPlaceSerializer;
	
	@Autowired
	private MailService mailService;
	
	@Value("${baseUri}")
	private String baseUri;
	
	@Value("${mergeNotificationMail}")
	private String mergeNotificationMail;

	@RequestMapping(value="/merge/{id1}/{id2}", method=RequestMethod.POST)
	public ModelAndView getPlace(@PathVariable String id1,
			@PathVariable String id2,
			HttpServletRequest request,
			HttpServletResponse response) {
				
		Place place1 = placeDao.findById(id1).orElse(null);
		Place place2 = placeDao.findById(id2).orElse(null);
		
		PlaceAccessService placeAccessService = new PlaceAccessService(recordGroupDao, groupRoleDao);
		
		if (!placeAccessService.hasEditAccess(place1) || !placeAccessService.hasEditAccess(place2))
			throw new IllegalStateException("Places may not be merged, as the user doesn't have the permission to edit both places.");
		
		if (!(place1.getRecordGroupId() == null && place2.getRecordGroupId() == null) && (place1.getRecordGroupId() != null && !place1.getRecordGroupId().equals(place2.getRecordGroupId())))
			throw new IllegalStateException("Places may not be merged, as they belong to different record groups.");
		
		// merge places
		Set<Place> updatedPlaces;
		try {
			updatedPlaces = merger.merge(place1, place2);
		} catch(Exception e) {
			throw new RuntimeException("Couldn't merge place " + place1.getId() + " with " + place2.getId(), e);
		}
		
		AncestorsHelper helper = new AncestorsHelper(placeDao);
		try {
			// update ancestor ids
			place1.setAncestors(helper.findAncestorIds(place1));
		} catch(Exception e) {
			throw new RuntimeException("Failed to set ancestors for merged place " + place1.getId(), e);
		}
		
		try {
			for (Place place : updatedPlaces) {
				placeDao.save(place);
				indexer.index(place);
			}
		} catch(Exception e) {
			throw new RuntimeException("Failed to save places affected by merge", e);
		}
		
		if (place1.getChildren() + place2.getChildren() < 10000)
			helper.updateAncestors(place1);
		
		// Delete place 2
		place2.setReplacedBy(place1.getId());
		place2.setDeleted(true);
		placeDao.save(place2);
		indexer.index(place2);

		changeRecordDao.save(createChangeRecord(place2, "replace", place1.getId()));
		changeRecordDao.save(createChangeRecord(place1, "merge", place2.getId()));
	
		logger.debug("finished merging " + place1.getId() + " and " + place2.getId() + " to " + place1.getId());
		
		try {
			sendNotificationMail(place1, place2, request);
		} catch (MessagingException e) {
			logger.warn("Could not send merge notification mail!", e);
		}
		
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
		mav.addObject("accessStatus", placeAccessService.getAccessStatus(place1));
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
	
	private void sendNotificationMail(Place place1, Place place2, HttpServletRequest request) throws MessagingException {
		
		if (mergeNotificationMail == null || mergeNotificationMail.isEmpty())
			return;
		
		String place1Name = (place1.getPrefName() != null) ? place1.getPrefName().getTitle() : "";
		String place2Name = (place2.getPrefName() != null) ? place2.getPrefName().getTitle() : "";
		
		String link = baseUri + "place/" + place1.getId();
		
		RequestContext context = new RequestContext(request);
		String subject = context.getMessage("mail.mergeNotification.subject", new Object[] { });
		String content = context.getMessage("mail.mergeNotification.content", new Object[] { place2Name, place2.getId(), link, place1Name, place1.getId() });
		
		mailService.sendMail(mergeNotificationMail, subject, content);
		logger.info("Sending merge notification mail to " + mergeNotificationMail);
	}
}
