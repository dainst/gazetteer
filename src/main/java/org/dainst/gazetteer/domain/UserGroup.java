package org.dainst.gazetteer.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class UserGroup {

	@Id
	private String id;
	
	private String name;	
	private Date creationDate;
	
	public UserGroup(String name) {
		this.setName(name);
		this.setCreationDate(new Date());
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	public String getCreationDateAsText() {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM.YYYY");
		
		if (creationDate == null)
			return "-";
		
		return dateFormat.format(creationDate);
	}
}
