package org.dainst.gazetteer.domain;

import org.springframework.data.mongodb.core.index.Indexed;


public class PlaceName {
	
	@Indexed
	private String title;
	
	private String language;
	
	private String script;
	
	private boolean ancient = false;
	
	private boolean transliterated = false;
	
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

	public boolean isAncient() {
		return ancient;
	}

	public void setAncient(boolean ancient) {
		this.ancient = ancient;
	}

	public boolean isTransliterated() {
		return transliterated;
	}

	public void setTransliterated(boolean transliterated) {
		this.transliterated = transliterated;
	}

	@Override
	public String toString() {
		return "PlaceName [title=" + title + ", language=" + language
				+ ", ancient=" + ancient + ", transliterated=" + transliterated + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime * result + (ancient ? 1231 : 1237);
		result = prime * result + (transliterated ? 1231 : 1237);
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlaceName other = (PlaceName) obj;
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (ancient != other.ancient)
			return false;
		if (transliterated != other.transliterated)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}
	
}
