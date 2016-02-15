package org.dainst.gazetteer.helpers;

import org.dainst.gazetteer.dao.GroupRoleRepository;
import org.dainst.gazetteer.dao.RecordGroupRepository;
import org.dainst.gazetteer.domain.GroupRole;
import org.dainst.gazetteer.domain.Place;
import org.dainst.gazetteer.domain.RecordGroup;
import org.dainst.gazetteer.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class PlaceAccessService {
	
	private RecordGroupRepository recordGroupDao;
	
	private GroupRoleRepository groupRoleDao;
	
	public enum AccessStatus {
		NONE,
		LIMITED_READ,		// Can not access complete coordinates of protected locations
		READ,
		EDIT
	}
	
	public PlaceAccessService(RecordGroupRepository recordGroupDao, GroupRoleRepository groupRoleDao) {
		
		this.recordGroupDao = recordGroupDao;
		this.groupRoleDao = groupRoleDao;
	}
	
	public AccessStatus getAccessStatus(Place place) {
		
		if (place.getRecordGroupId() == null || place.getRecordGroupId().isEmpty())
			return AccessStatus.EDIT;
		
		RecordGroup group = recordGroupDao.findOne(place.getRecordGroupId());
		
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		if (user != null) {		
			GroupRole role = groupRoleDao.findByGroupIdAndUserId(place.getRecordGroupId(), user.getId());
		
			if (role != null) {
				if (role.getRoleType().equals("admin") || role.getRoleType().equals("edit"))
					return AccessStatus.EDIT;
				else if (role.getRoleType().equals("read"))
					return AccessStatus.READ;
				else if (role.getRoleType().equals("limitedRead"))
					return AccessStatus.LIMITED_READ;
				else
					return AccessStatus.NONE;
			} else if (group.getShowPlaces())
				return AccessStatus.LIMITED_READ;
			else
				return AccessStatus.NONE;
		} else if (group.getShowPlaces())
			return AccessStatus.LIMITED_READ;
		else
			return AccessStatus.NONE;
	}
	
	public boolean hasReadAccess(Place place) {
		
		return hasReadAccess(getAccessStatus(place));
	}
	
	public static boolean hasReadAccess(AccessStatus accessStatus) {
		
		switch(accessStatus) {
		case LIMITED_READ:
		case READ:
		case EDIT:
			return true;
		default:
			return false;
		}
	}
	
	public boolean hasEditAccess(Place place) {
		
		return hasEditAccess(getAccessStatus(place));
	}
	
	public static boolean hasEditAccess(AccessStatus accessStatus) {
		
		return accessStatus.equals(AccessStatus.EDIT);
	}
}
