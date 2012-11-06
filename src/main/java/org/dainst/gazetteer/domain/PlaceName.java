package org.dainst.gazetteer.domain;


public class PlaceName {
	
	private String title;
	
	private String language;
	
	private String script;
	
	private boolean modern = true;
	
	private int ordering = 0;
	
	public PlaceName() {
	}
	
	public PlaceName(String title) {
		this.title = title;
	}
	
	public PlaceName(String title, String language) {
		this.title = title;
		this.language = language;
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
