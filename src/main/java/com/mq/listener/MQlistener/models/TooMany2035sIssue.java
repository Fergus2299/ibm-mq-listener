package com.mq.listener.MQlistener.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TooMany2035sIssue extends Issue {
    
    // Constant to hold the filename for logging
    private static final String LOG_FILE_NAME = "issues_log_2.txt";

    // List to hold error rates for each window
//    private List<WindowData> windowDataList;
    
    private Map<String, AuthErrorDetails> windowDataMap;

    // Boolean to determine if the issue is still active
    private boolean issueActive;

    /**
     * Constructor to initialize the TooMany2035sIssue with default values.
     *
     * @param queueName The name of the queue.
     */
    public TooMany2035sIssue(String MQObjectType, String MQObjectName) {
        this.issueCode = "Too_Many_2035s";
        this.startTimeStamp = formatNow();
        this.generalDesc = "";
        this.technicalDetails = "";
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
//        this.windowDataList = new ArrayList<>();
        this.windowDataMap = new LinkedHashMap<>();
        this.issueActive = true;
    }
 
    public void addWindowDataType2(String startTime, double rate, String appName, String userId) {
        System.out.println("Adding window data with start time);
        WindowDataType2 data = new WindowDataType2(startTime, rate, appName, userId);
        windowDataMap.put(startTime, data);
    }
    public void addWindowDataType1(String startTime, double rate, String appName, String userId) {
        System.out.println("Adding window data with start time: " + startTime + " and rate: " + rate + ", App Name: " + appName + ", User ID: " + userId);
        WindowDataType2 data = new WindowDataType2(startTime, rate, appName, userId);
        windowDataMap.put(startTime, data);
    }


    public void closeLatestWindow(String endTime) {
        System.out.println("Closing latest window with end time: " + endTime);
        if (!windowDataMap.isEmpty()) {
            // Get the last key in the LinkedHashMap (latest window)
            String latestStartTime = null;
            for (String startTime : windowDataMap.keySet()) {
                latestStartTime = startTime;
            }

            if (latestStartTime != null) {
                WindowDataType2 latestData = windowDataMap.get(latestStartTime);
                latestData.setEndTime(endTime);
            }
        } else {
            System.out.println("Warning: Attempted to close a window, but windowDataMap is empty.");
        }
    }

    /**
     * Checks if the issue is currently active.
     *
     * @return boolean indicating if the issue is active.
     */
    public boolean isIssueActive() {
        return this.issueActive;
    }

    /**
     * Closes the issue and logs its details to a file.
     */
    public void closeIssue() {
    	System.out.println("Preparing to log issue.");
        
        // Get the current issue report
//        String issueReport = getIssueReport();
        System.out.println("Current Issue Details:");
//        System.out.println(issueReport);
        System.out.println("-------------------------------");
        this.endTimestamp = formatNow();
        this.issueActive = false;
//        logToFile();
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

    /**
     * Writes the issue details to a log file.
     */
//    private void logToFile() {
//        System.out.println("Attempting to log issue to file.");
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_NAME, true))) {
//            writer.write("Issue Code: " + this.issueCode);
//            writer.newLine();
//
//            writer.write("Start Time: " + this.startTimeStamp);
//            writer.newLine();
//
//            writer.write("End Time: " + this.endTimestamp);
//            writer.newLine();
//
//
//            writer.write("Window Reports:");
//            writer.newLine();
//
//            for (WindowData data : windowDataList) {
//                writer.write(data.toString());
//                writer.newLine();
//            }
//
//            writer.write("--------------------------");
//            writer.newLine();
//
//            System.out.println("Successfully logged issue details.");
//        } catch (IOException e) {
//            System.err.println("Failed to write to log file: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    /**
     * Generates a full report of the issue including details and error rates for each window.
     *
     * @return String representation of the issue and its window data.
     */
    public String getIssueReport() {
        StringBuilder report = new StringBuilder();

        report.append("Issue Code: ").append(this.issueCode).append("\n");
        report.append("Start Time: ").append(this.startTimeStamp).append("\n");
        if (endTimestamp != null) {
            report.append("End Time: ").append(this.endTimestamp).append("\n");
        }
        report.append("Issue Active: ").append(this.issueActive).append("\n");

        if (windowDataMap.isEmpty()) {
            report.append("No windows data available.\n");
        } else {
            report.append("Window Reports:\n");
            for (WindowDataType2 data : windowDataMap.values()) {
                report.append(data.toString()).append("\n");
            }
        }

        return report.toString();
    }

    

    
    

    /**
     * Inner class to represent data for each window.
     * Is added to WindowsDataList
     */
    private class WindowDataType2 {
        private String startTime;
        private String endTime;
        private double rate;
        
        private String appName;
        private String userId;

        public WindowDataType2(String startTime, double rate, String appName, String userId) {
            this.startTime = startTime;
            this.rate = rate;
            this.appName = appName;
            this.userId = userId;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "Window: [" + startTime + (endTime != null ? " - " + endTime : "") 
                 + "], Rate: " + rate + " errors/minute, App Name: " + appName + ", User ID: " + userId;
        }
    }
    
    private class WindowDataType1 {
        private String startTime;
        private String endTime;
        private double rate;
        
        private String appName;
        private String userId;

        public WindowDataType1(String startTime, 
        		double rate, 
        		String appName, 
        		String userId,
        		String channelName,
        		String connName,
        		String CSPUserId) {
        	
            this.startTime = startTime;
            this.rate = rate;
            this.appName = appName;
            this.userId = userId;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "Window: [" + startTime + (endTime != null ? " - " + endTime : "") 
                 + "], Rate: " + rate + " errors/minute, App Name: " + appName + ", User ID: " + userId;
        }
    }
}