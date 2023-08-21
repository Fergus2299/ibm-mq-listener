package com.mq.listener.MQlistener.models;

import TimeFormatter.TimeFormatter;

public class AuthErrorDetails {

	private String startTime;
	private int count;
	private String userId;
	private String appName;
	
	public AuthErrorDetails(int count, String userId, String appName) {
	this.startTime = TimeFormatter.formatNow();
    this.count = count;
    this.userId = userId;
    this.appName = appName;
    }

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}
	
	//////////////////////////////
    public void incrementCount() {
        this.count++;
    }

	
}    

