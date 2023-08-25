package com.mq.listener.MQlistener.models.Errors;

import java.util.Map;

import TimeFormatter.TimeFormatter;

public class Auth1ErrorDetails extends AuthErrorDetails {

    private String channelName;
    private String connName;
    private String CSPUserId;

    public Auth1ErrorDetails(
    		int count, 
    		String userId, 
    		String appName,
    		String channelName, 
    		String connName, 
    		String CSPUserId) {
    	// has the getters and setters from the parent class
        super(count, userId, appName);
        this.channelName = channelName;
        this.connName = connName;
        this.CSPUserId = CSPUserId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getConnName() {
        return connName;
    }

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public String getCSPUserId() {
        return CSPUserId;
    }

    public void setCSPUserId(String CSPUserId) {
        this.CSPUserId = CSPUserId;
    }
    
    @Override
    public String toString() {
        return super.toString().replace("}", ", ") + 
               "channelName='" + channelName + '\'' +
               ", connName='" + connName + '\'' +
               ", CSPUserId='" + CSPUserId + '\'' +
               '}';
    }
    
    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> map = super.toHashMap();
        map.put("channelName", this.channelName);
        map.put("connName", this.connName);
        map.put("CSPUserId", this.CSPUserId);
        return map;
    }
    
    public void reset() {
        this.setCount(0);
        this.setUserId("");
        this.setAppName("");
        this.channelName = "";
        this.connName = "";
        this.CSPUserId = "";
    }
}