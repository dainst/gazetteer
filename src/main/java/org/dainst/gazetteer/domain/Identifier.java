package org.dainst.gazetteer.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Identifier {

	private long id;
	private String value;	
	private String context;
	
	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
}
