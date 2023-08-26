package com.mq.listener.MQlistener.models.Errors;

import java.util.List;
import java.util.Map;

public class UnknownObjectErrorDetails extends ErrorDetails {

    private String connName;
    private String channelName;
    private String QName;

    public UnknownObjectErrorDetails(
    		int count, 
    		String appName,
    		String connName, 
    		String channelName, 
    		String QName) {
        super(count, appName);
        this.connName = connName;
        this.channelName = channelName;
        this.QName = QName;
    }


    public String getConnName() {
        return connName;
    }

    public void setConnName(String connName) {
        this.connName = connName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getQName() {
        return QName;
    }

    public void setQName(String QName) {
        this.QName = QName;
    }

    @Override
    public String toString() {
        return super.toString().replace("}", "") +
               ", connName='" + connName + '\'' +
               ", channelName='" + channelName + '\'' +
               ", QName='" + QName + '\'' +
               " }";
    }

    @Override
    public Map<String, Object> toHashMap() {
        Map<String, Object> map = super.toHashMap();
        map.put("connName", this.connName);
        map.put("channelName", this.channelName);
        map.put("QName", this.QName);
        return map;
    }
}