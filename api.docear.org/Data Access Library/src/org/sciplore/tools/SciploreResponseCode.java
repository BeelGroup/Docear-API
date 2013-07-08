package org.sciplore.tools;

public class SciploreResponseCode {
	
	public static final int INVALID_USER_TYPE = 423;
	public static final int PASSWORDS_NOT_IDENTICAL = 442;
	public static final int PASSWORD_TOO_SHORT = 441;
	public static final int EMAIL_INVALID = 430;
	public static final int EMAIL_ALREADY_EXISTS = 431;
	public static final int USERNAME_INVALID = 420;
	public static final int USERNAME_NOT_AVAILABLE = 421;
	public static final int OK = 200;
	public static final int UNAUTHORIZED = 401;
	public static final int BAD_REQUEST = 400;
	public static final int INTERNAL_SERVER_ERROR = 500;
	
	private int responseCode;
	private String responseMessage;	
	
	public SciploreResponseCode(int responseCode, String resonseMessage) {		
		this.responseCode = responseCode;
		this.responseMessage = resonseMessage;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String resonseMessage) {
		this.responseMessage = resonseMessage;
	}

}
