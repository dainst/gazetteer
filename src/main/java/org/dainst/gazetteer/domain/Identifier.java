package org.dainst.gazetteer.domain;

public class Identifier {

	private String value;	
	private String context;
	
	public Identifier() {
		
	}
	
	public Identifier(String value, String context) {
		this.value = value;
		this.context = context;
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
	
	public String toString() {
		return String.format("Identifier { value: %s, context: %s}", value, context); 
	}
	
}
