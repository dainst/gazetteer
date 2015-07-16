package org.dainst.gazetteer.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class GroupRole {
	
	@Id
	private String id;
		
	private String groupId;
	private String userId;
	private String roleType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupRole other = (GroupRole) obj;
		if (id == null && other.id != null)
			return false;
		if (id != null && !id.equals(other.id))
			return false;
		if (groupId == null && other.groupId != null)
			return false;
		if (groupId != null && !groupId.equals(other.groupId))
			return false;
		if (userId == null && other.userId != null)
			return false;
		if (userId != null && !userId.equals(other.userId))
			return false;
		if (roleType == null && other.roleType != null)
			return false;
		if (roleType != null && !roleType.equals(other.roleType))
			return false;
		return true;
	}
}
