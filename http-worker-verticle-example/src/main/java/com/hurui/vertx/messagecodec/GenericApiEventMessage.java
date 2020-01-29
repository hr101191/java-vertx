package com.hurui.vertx.messagecodec;

public class GenericApiEventMessage {
	private String jsonReqString;
	private String jsonRespString;
	private String errorMessage;
	private String stackTraceMessageString;
	private int httpStatusCode;
	
	public String getJsonReqString() {
		return jsonReqString;
	}
	public void setJsonReqString(String jsonReqString) {
		this.jsonReqString = jsonReqString;
	}
	
	public String getJsonRespString() {
		return jsonRespString;
	}
	public void setJsonRespString(String jsonRespString) {
		this.jsonRespString = jsonRespString;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getStackTraceMessageString() {
		return stackTraceMessageString;
	}
	public void setStackTraceMessageString(String stackTraceMessageString) {
		this.stackTraceMessageString = stackTraceMessageString;
	}
	
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
}
