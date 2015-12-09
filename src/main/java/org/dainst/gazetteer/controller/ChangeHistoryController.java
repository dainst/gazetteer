package org.dainst.gazetteer.controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.dainst.gazetteer.dao.PlaceChangeRecordRepository;
import org.dainst.gazetteer.dao.PlaceRepository;
import org.dainst.gazetteer.dao.UserRepository;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.PlaceChangeRecord;
import org.dainst.gazetteer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContext;


@Controller
public class ChangeHistoryController {
	
	@Autowired
	private PlaceChangeRecordRepository changeRecordRepository;
	
	@Autowired
	private PlaceRepository placeRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Value("${version}")
	private String version;
		
	private int recordsPerPage = 15;

	
	@RequestMapping(value="/globalChangeHistory")
	public String getGlobalChangeHistory(HttpServletRequest request, @RequestParam(required=false) String sort, @RequestParam(required=false) boolean isDescending,
										 @RequestParam(required=false) Integer page, @RequestParam(required=false) String startDate,
										 @RequestParam(required=false) String endDate, ModelMap model) {
		
		boolean isAdmin = false;		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if (authority.getAuthority().equals("ROLE_ADMIN")) {
				isAdmin = true;
			}
		}
		
		User user = (User) authentication.getPrincipal();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		Calendar calendar = Calendar.getInstance();
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				
		Date start = calendar.getTime();
		Date end = new Date();
		
		if (startDate != null && endDate != null) {
		
			try {
				start = format.parse(startDate);
				end = format.parse(endDate);
			} catch (ParseException e) {
				// keep dates (= this week)
			}
		}
		
		List<PlaceChangeRecord> changeHistory;
		
		if (isAdmin)
			changeHistory = (List<PlaceChangeRecord>) changeRecordRepository.findAll();
		else
			changeHistory = (List<PlaceChangeRecord>) changeRecordRepository.findByUserId(user.getId());
		
		List<PresentablePlaceChangeRecord> presChangeHistory = new ArrayList<PresentablePlaceChangeRecord>();
		
		Calendar calendarStart = Calendar.getInstance();
		Calendar calendarEnd = Calendar.getInstance();		
		calendarStart.setTime(start);
		calendarEnd.setTime(end);
		
		RequestContext context = new RequestContext(request);
		
		for (PlaceChangeRecord changeRecord : changeHistory) {
			
			Calendar calendarChangeDate = Calendar.getInstance();
			calendarChangeDate.setTime(changeRecord.getChangeDate());
			
			boolean sameAsStart = calendarStart.get(Calendar.YEAR) == calendarChangeDate.get(Calendar.YEAR) &&
					calendarStart.get(Calendar.DAY_OF_YEAR) == calendarChangeDate.get(Calendar.DAY_OF_YEAR);
			
			boolean sameAsEnd = calendarEnd.get(Calendar.YEAR) == calendarChangeDate.get(Calendar.YEAR) &&
					calendarEnd.get(Calendar.DAY_OF_YEAR) == calendarChangeDate.get(Calendar.DAY_OF_YEAR);
			
			if ((sameAsStart || changeRecord.getChangeDate().after(start)) &&
				(sameAsEnd || changeRecord.getChangeDate().before(end))) {
				
				PresentablePlaceChangeRecord presChangeRecord = new PresentablePlaceChangeRecord();
				presChangeRecord.setChangeDate(changeRecord.getChangeDate());
				
				User changeRecordUser = userRepository.findById(changeRecord.getUserId());
				if (changeRecordUser != null) {
					presChangeRecord.setUserId(changeRecord.getUserId());
					presChangeRecord.setUsername(changeRecordUser.getUsername());
				}
				else {
					presChangeRecord.setUserId(null);
					presChangeRecord.setUsername(context.getMessage("ui.changeHistory.deletedUser"));
				}
				presChangeRecord.setPlaceId(changeRecord.getPlaceId());
				
				Place place = placeRepository.findOne(changeRecord.getPlaceId());
				if (place == null) {
					presChangeRecord.setPlacename(context.getMessage("ui.changeHistory.completelyDeletedPlace"));
					presChangeRecord.setNotFound(true);
				}
				else if (place.getPrefName() != null)
					presChangeRecord.setPlacename(place.getPrefName().getTitle());
				
				presChangeRecord.setChangeType(changeRecord.getChangeType());
				presChangeRecord.setAdditionalData(changeRecord.getAdditionalData());
				presChangeHistory.add(presChangeRecord);
			}
		}
		
		if (sort == null || sort.equals("")) {
			if (isDescending)
				Collections.sort(presChangeHistory, Collections.reverseOrder(new ChangeDateComparator()));
			else
				Collections.sort(presChangeHistory, new ChangeDateComparator());
		}
		else {
			switch (sort) {
			case "username":
				if (isDescending)
					Collections.sort(presChangeHistory, Collections.reverseOrder(new UsernameComparator()));
				else
					Collections.sort(presChangeHistory, new UsernameComparator());			
				break;
			case "placeId":
				if (isDescending)
					Collections.sort(presChangeHistory, Collections.reverseOrder(new PlaceIdComparator()));
				else
					Collections.sort(presChangeHistory, new PlaceIdComparator());	
				break;
			case "placename":
				if (isDescending)
					Collections.sort(presChangeHistory, Collections.reverseOrder(new PlacenameComparator()));
				else
					Collections.sort(presChangeHistory, new PlacenameComparator());	
				break;
			case "changeType":
				if (isDescending)
					Collections.sort(presChangeHistory, Collections.reverseOrder(new ChangeTypeComparator()));
				else
					Collections.sort(presChangeHistory, new ChangeTypeComparator());	
				break;
			}
		}
		
		int pages = (presChangeHistory.size() + recordsPerPage - 1) / recordsPerPage;
		
		if (page == null || page < 0)
			page = 0;
		
		if (page >= pages && pages != 0)
			page = pages - 1;
		
		int toIndex = page * recordsPerPage + recordsPerPage;
		if (toIndex > presChangeHistory.size())
			toIndex = presChangeHistory.size();

		presChangeHistory = presChangeHistory.subList(page * recordsPerPage, toIndex);
		
		SimpleDateFormat presFormat = new SimpleDateFormat("dd.MM.YYYY");
		
		if (pages == 0)
			pages = 1;
		
		model.addAttribute("page", page);
		model.addAttribute("pages", pages);
		model.addAttribute("changes", presChangeHistory);		
		model.addAttribute("isDescending", isDescending);
		model.addAttribute("lastSorting", sort);
		model.addAttribute("startDate", startDate);
		model.addAttribute("endDate", endDate);
		model.addAttribute("startDatePres", presFormat.format(start));
		model.addAttribute("endDatePres", presFormat.format(end));
		model.addAttribute("version", version);
		
		return "globalChangeHistory";
	}
		
	public class PresentablePlaceChangeRecord {
		
		private Date changeDate;
		
		private String userId;
		private String username;
		
		private String placeId;
		private String placename;
		
		private String changeType;
		
		private String additionalData;
		
		private boolean notFound = false;
			
		public Date getChangeDate() {
			return changeDate;
		}
		
		public String getChangeDateAsText() {
			DateFormat format = new SimpleDateFormat("dd.MM.yyyy (HH:mm:ss z)");
			
			return format.format(changeDate);
		}
		
		public void setChangeDate(Date changeDate) {
			this.changeDate = changeDate;
		}
		
		public String getUserId() {
			return userId;
		}

		public void setUserId(String userId) {
			this.userId = userId;
		}

		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getPlaceId() {
			return placeId;
		}

		public void setPlaceId(String placeId) {
			this.placeId = placeId;
		}
		
		public String getPlacename() {
			return placename;
		}
		
		public void setPlacename(String placename) {
			this.placename = placename;
		}

		public String getChangeType() {
			if (changeType != null)
				return changeType;
			else
				return "unknown";
		}

		public void setChangeType(String changeType) {
			this.changeType = changeType;
		}
		
		public String getAdditionalData() {
			return additionalData;
		}

		public void setAdditionalData(String additionalData) {
			this.additionalData = additionalData;
		}

		public boolean isNotFound() {
			return notFound;
		}

		public void setNotFound(boolean notFound) {
			this.notFound = notFound;
		}
	}
	
	public static class ChangeDateComparator implements Comparator<PresentablePlaceChangeRecord> {
		public int compare(PresentablePlaceChangeRecord changeRecord1, PresentablePlaceChangeRecord changeRecord2) {
			long time1 = changeRecord1.getChangeDate().getTime();
			long time2 = changeRecord2.getChangeDate().getTime();
			
			if (time2 > time1)
	            return 1;
			else if (time1 > time2)
	            return -1;
			else
	            return 0;
		}
	}
	
	public static class UsernameComparator implements Comparator<PresentablePlaceChangeRecord> {
		public int compare(PresentablePlaceChangeRecord changeRecord1, PresentablePlaceChangeRecord changeRecord2) {
			if (changeRecord1.getUsername() == null && changeRecord2.getUsername() == null)
				return 0;
			if (changeRecord1.getUsername() == null && changeRecord2.getUsername() != null)
				return 1;
			if (changeRecord1.getUsername() != null && changeRecord2.getUsername() == null)
				return -1;
			
			return changeRecord1.getUsername().toLowerCase().compareTo(changeRecord2.getUsername().toLowerCase());
		}
	}
	
	public static class PlaceIdComparator implements Comparator<PresentablePlaceChangeRecord> {
		public int compare(PresentablePlaceChangeRecord changeRecord1, PresentablePlaceChangeRecord changeRecord2) {
			if (changeRecord1.getPlaceId() == null && changeRecord2.getPlaceId() == null)
				return 0;
			if (changeRecord1.getPlaceId() == null && changeRecord2.getPlaceId() != null)
				return 1;
			if (changeRecord1.getPlaceId() != null && changeRecord2.getPlaceId() == null)
				return -1;
			
			return changeRecord1.getPlaceId().toLowerCase().compareTo(changeRecord2.getPlaceId().toLowerCase());
		}
	}
	
	public static class PlacenameComparator implements Comparator<PresentablePlaceChangeRecord> {
		public int compare(PresentablePlaceChangeRecord changeRecord1, PresentablePlaceChangeRecord changeRecord2) {
			if (changeRecord1.getPlacename() == null && changeRecord2.getPlacename() == null)
				return 0;
			if (changeRecord1.getPlacename() == null && changeRecord2.getPlacename() != null)
				return 1;
			if (changeRecord1.getPlacename() != null && changeRecord2.getPlacename() == null)
				return -1;
			
			return changeRecord1.getPlacename().toLowerCase().compareTo(changeRecord2.getPlacename().toLowerCase());
		}
	}
	
	public static class ChangeTypeComparator implements Comparator<PresentablePlaceChangeRecord> {
		public int compare(PresentablePlaceChangeRecord changeRecord1, PresentablePlaceChangeRecord changeRecord2) {
			if (changeRecord1.getChangeType() == null && changeRecord2.getChangeType() == null)
				return 0;
			if (changeRecord1.getChangeType() == null && changeRecord2.getChangeType() != null)
				return 1;
			if (changeRecord1.getChangeType() != null && changeRecord2.getChangeType() == null)
				return -1;
			
			return changeRecord1.getChangeType().toLowerCase().compareTo(changeRecord2.getChangeType().toLowerCase());
		}
	}
}