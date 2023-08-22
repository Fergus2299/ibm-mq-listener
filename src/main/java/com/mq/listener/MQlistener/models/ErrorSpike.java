package com.mq.listener.MQlistener.models;


import java.util.Map;

import TimeFormatter.TimeFormatter;


public class ErrorSpike extends Issue {
    public ErrorSpike(String issueCode, String MQObjectType, String MQObjectName) {
        this.issueCode = issueCode;
        this.startTimeStamp = TimeFormatter.formatNow();
        this.generalDesc = "";
        this.technicalDetails = "";
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
    }
    
    // puts window data into the map
    public void addWindowData(ErrorDetails details, Double rate) {
//        String currentTimeKey = TimeFormatter.formatNow();
        Map<String, Object> detailsHashMap = details.toHashMap();
        detailsHashMap.put("Rate", rate);
        detailsHashMap.put("LogTime", TimeFormatter.formatNow());
        
        if (!this.technicalDetails.isEmpty()) {
            this.technicalDetails += ", ";
        }
        this.technicalDetails += detailsHashMap.toString();
    
    }

    public void printIssueDetails() {
        System.out.println("----------- Issue Details -----------");
        System.out.println("Issue Code: " + this.issueCode);
        System.out.println("Start Timestamp: " + this.startTimeStamp);
        
        if(this.endTimestamp != null) { // Print the end timestamp if it exists
            System.out.println("End Timestamp: " + this.endTimestamp);
        }
        
        System.out.println("MQ Object Type: " + this.MQObjectType);
        System.out.println("MQ Object Name: " + this.MQObjectName);
        
        System.out.println("General Description: " + this.generalDesc);
        System.out.println("Technical Details:" + this.technicalDetails);
        }

    
    @Override
    public void closeIssue() {
        System.out.println("Preparing to log issue.");

        super.closeIssue();

        System.out.println("Current Issue Details:");
        printIssueDetails();
        System.out.println("-------------------------------");
    }

}