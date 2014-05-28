package org.dainst.gazetteer.domain;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document
public class UserPasswordChangeRequest {

	@Id
	private String id;
	
	private String userId;
	private Date requestDate;
	private String resetKey;
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getUserId() {
		return userId;
	}
	
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Date getRequestDate() {
		return requestDate;
	}
	
	public void setRequestDate(Date requestDate) {
		this.requestDate = requestDate;
	}
	
	public String getResetKey() {
		return resetKey;
	}
	
	public void setResetKey(String resetKey) {
		this.resetKey = resetKey;
	}
}