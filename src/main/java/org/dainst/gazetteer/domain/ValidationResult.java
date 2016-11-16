package org.dainst.gazetteer.domain;

public class ValidationResult {
	
	private boolean success = false;
	private String message = "";
	private String messageKey = "";
	private String messageData = "";
	
	public ValidationResult(boolean success) {
		this(success, "", "", "");
	}
	
	public ValidationResult(boolean success, String message) {
		this(success, message, "", "");
	}
	
	public ValidationResult(boolean success, String message, String messageKey, String messageData) {
		this.success = success;
		this.message = message;
		this.messageKey = messageKey;
		this.messageData = messageData;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}
	
	public String getMessageKey() {
		return messageKey;
	}

	public String getMessageData() {
		return messageData;
	}

}
