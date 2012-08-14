package org.dainst.gazetteer.domain;

import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
	
	private boolean success = true;
	private String message;
	private Map<String,String> messages = new HashMap<String,String>();

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	public Map<String, String> getMessages() {
		return messages;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public void setMessages(Map<String, String> messages) {
		this.messages = messages;
	}
	
	public void addMessage(String key, String message) {
		this.messages.put(key, message);
	}

}
