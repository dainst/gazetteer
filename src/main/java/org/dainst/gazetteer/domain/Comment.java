package org.dainst.gazetteer.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Comment {
	
	private long id;
	private String text;	
	private String language;
	
	@Id
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getLanguage() {
		return language;
	}
	
	public void setLanguage(String language) {
		this.language = language;
	}

}
