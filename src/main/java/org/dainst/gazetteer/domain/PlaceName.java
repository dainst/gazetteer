package org.dainst.gazetteer.domain;

import java.util.Date;

public class PlaceName {
	
	private String title;
	
	private String language;
	
	private String script;
	
	private boolean modern = true;
	
	private int ordering = 0;
	
	private Date created;
	
	public PlaceName() {
		created = new Date();
	}
	
	public PlaceName(String title) {
		this.title = title;
		created = new Date();
	}
	
	public PlaceName(String title, String language) {
		this.title = title;
		this.language = language;
		created = new Date();
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public boolean isModern() {
		return modern;
	}

	public void setModern(boolean modern) {
		this.modern = modern;
	}

	public int getOrdering() {
		return ordering;
	}

	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}
	
}
