package com.mq.listener.MQlistener.models.Issue;


import java.util.Map;

import com.mq.listener.MQlistener.models.Errors.ErrorDetails;

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

    
    @Override
    public void closeIssue() {
        System.out.println("Preparing to log issue.");

        super.closeIssue();

        System.out.println("Current Issue Details:");
        printIssueDetails();
        System.out.println("-------------------------------");
    }

}