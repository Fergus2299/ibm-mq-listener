package com.mq.listener.MQlistener.models.Errors;

import java.util.HashMap;
import java.util.Map;

public class ErrorDetails {
    
    protected int count;
    protected String appName;

    public ErrorDetails(int count, String appName) {
        this.count = count;
        this.appName = appName;
    }
   

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void incrementCount() {
        this.count++;
    }

    @Override
    public String toString() {
        return " { " +
               "count=" + count +
               ", appName='" + appName + '\'' +
               " }";
    }

    public Map<String, Object> toHashMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("count", this.count);
        map.put("appName", this.appName);
        return map;
    }

    public void printSelf() {
        System.out.println(this.toString());
    }
}