package com.mq.listener.MQlistener.models;

import java.util.HashMap;
import java.util.Map;

import TimeFormatter.TimeFormatter;

public class AuthErrorDetails {

	private int count;
	private String userId;
	private String appName;
	
	public AuthErrorDetails(int count, String userId, String appName) {
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


	@Override
    public String toString() {
        return " { "  +
               "count=" + count +
               ", userId='" + userId + '\'' +
               ", appName='" + appName + '\'' +
               " }";
    }
	public Map<String, Object> toHashMap() {
	    Map<String, Object> map = new HashMap<>();
	    map.put("count", this.count);
	    map.put("userId", this.userId);
	    map.put("appName", this.appName);
	    return map;
	}

    public void printSelf() {
        System.out.println(this.toString());
    }


}    

