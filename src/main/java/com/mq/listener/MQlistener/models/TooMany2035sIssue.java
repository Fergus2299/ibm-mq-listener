package com.mq.listener.MQlistener.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import TimeFormatter.TimeFormatter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TooMany2035sIssue extends Issue {
    
    // Constant to hold the filename for logging
    private static final String LOG_FILE_NAME = "issues_log_2.txt";
    
    

    public TooMany2035sIssue(String MQObjectType, String MQObjectName) {
        this.issueCode = "Too_Many_2035s";
        this.startTimeStamp = formatNow();
        this.generalDesc = "";
        this.technicalDetails = "";
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
//        this.windowDataList = new ArrayList<>();
    }
    
    // puts window data into the map
    public void addWindowData(AuthErrorDetails details, Double rate) {
//        String currentTimeKey = TimeFormatter.formatNow();
        Map<String, Object> detailsHashMap = details.toHashMap();
        detailsHashMap.put("Rate", rate);
        detailsHashMap.put("LogTime", TimeFormatter.formatNow());
        
        if (!this.technicalDetails.isEmpty()) {
            this.technicalDetails += ", ";
        }
        this.technicalDetails += detailsHashMap.toString();
    
    }



    /**
     * Formats the current date and time in ISO_LOCAL_DATE_TIME format.
     *
     * @return The current date and time as a formatted string.
     */
    public String formatNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return now.format(formatter);
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

        // Call the superclass's closeIssue method to ensure the issue is logged using LoggingService
        super.closeIssue();

        // Continue with any other code specific to TooMany2035sIssue
        System.out.println("Current Issue Details:");
        printIssueDetails();
        System.out.println("-------------------------------");
    }

}