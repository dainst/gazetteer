package org.dainst.gazetteer.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class RecordGroup {

	@Id
	private String id;
	
	private String name;
	private boolean showPlaces;
	private Date creationDate;
	
	public RecordGroup(String name) {
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

	public boolean getShowPlaces() {
		return showPlaces;
	}

	public void setShowPlaces(boolean showPlaces) {
		this.showPlaces = showPlaces;
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RecordGroup other = (RecordGroup) obj;
		if (id == null && other.id != null)
			return false;
		if (id != null && !id.equals(other.id))
			return false;
		if (name == null && other.name != null)
			return false;
		if (name != null && !name.equals(other.name))
			return false;
		if (showPlaces != other.showPlaces)
			return false;
		if (creationDate == null && other.creationDate != null)
			return false;
		if (creationDate != null && !creationDate.equals(other.creationDate))
			return false;
		return true;
	}
}
