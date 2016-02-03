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
	
	public PlaceAccessService(RecordGroupRepository recordGroupDao, GroupRoleRepository groupRoleDao) {
		
		this.recordGroupDao = recordGroupDao;
		this.groupRoleDao = groupRoleDao;
	}
	
	public boolean checkPlaceAccess(Place place, boolean editAccessRequired) {
		
		if (place.getRecordGroupId() == null || place.getRecordGroupId().isEmpty())
			return true;
		
		RecordGroup group = recordGroupDao.findOne(place.getRecordGroupId());
		
		if (group.getShowPlaces() && !editAccessRequired)
			return true;		
		
		User user = null;
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof User)
			user = (User) principal;
		
		if (user == null)
			return false;
		
		GroupRole role = groupRoleDao.findByGroupIdAndUserId(place.getRecordGroupId(), user.getId());
		
		if (role != null && (role.getRoleType().equals("admin") || role.getRoleType().equals("edit")
				|| (role.getRoleType().equals("read") && !editAccessRequired)))
			return true;
		else
			return false;
	}
}
