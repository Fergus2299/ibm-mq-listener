package com.mq.listener.MQlistener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class TooMany2035sIssue extends Issue {
    
    // Constant to hold the filename for logging
    private static final String LOG_FILE_NAME = "issues_log_2.txt";

    // List to hold error rates for each window
    private List<WindowData> windowDataList;

    // Boolean to determine if the issue is still active
    private boolean issueActive;

    /**
     * Constructor to initialize the TooMany2035sIssue with default values.
     *
     * @param queueName The name of the queue.
     */
    public TooMany2035sIssue(String queueName) {
        this.issueCode = "Too_Many_2035s";
        this.startTimeStamp = formatNow();
        this.Q = queueName;
        this.windowDataList = new ArrayList<>();
        this.issueActive = true;
        System.out.println("Initialized TooMany2035sIssue for queue: " + queueName);
    }

    /**
     * Adds error rate data for a new window.
     *
     * @param startTime The start time of the window.
     * @param rate The error rate for the window.
     */
    public void addWindowData(String startTime, double rate) {
        System.out.println("Adding window data with start time: " + startTime + " and rate: " + rate);
        WindowData data = new WindowData(startTime, rate);
        windowDataList.add(data);
        
    }

    /**
     * Closes the latest window by setting its end time.
     *
     * @param endTime The end time of the latest window.
     */
    public void closeLatestWindow(String endTime) {
        System.out.println("Closing latest window with end time: " + endTime);
        if (!windowDataList.isEmpty()) {
            WindowData latestData = windowDataList.get(windowDataList.size() - 1);
            latestData.setEndTime(endTime);
        } else {
            System.out.println("Warning: Attempted to close a window, but windowDataList is empty.");
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
        String issueReport = getIssueReport();
        System.out.println("Current Issue Details:");
        System.out.println(issueReport);
        System.out.println("-------------------------------");
        this.endTimestamp = formatNow();
        this.issueActive = false;
        logToFile();
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
    private void logToFile() {
        System.out.println("Attempting to log issue to file.");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_NAME, true))) {
            writer.write("Issue Code: " + this.issueCode);
            writer.newLine();

            writer.write("Start Time: " + this.startTimeStamp);
            writer.newLine();

            writer.write("End Time: " + this.endTimestamp);
            writer.newLine();

            writer.write("Queue Name: " + this.Q);
            writer.newLine();

            writer.write("Window Reports:");
            writer.newLine();

            for (WindowData data : windowDataList) {
                writer.write(data.toString());
                writer.newLine();
            }

            writer.write("--------------------------");
            writer.newLine();

            System.out.println("Successfully logged issue details.");
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
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
        report.append("Queue Name: ").append(this.Q).append("\n");
        report.append("Issue Actice: ").append(this.issueActive).append("\n");

        if (windowDataList.isEmpty()) {
            report.append("No windows data available.\n");
        } else {
            report.append("Window Reports:\n");
            for (WindowData data : windowDataList) {
                report.append(data.toString()).append("\n");
            }
        }

        return report.toString();
    }

    /**
     * Inner class to represent data for each window.
     * Is added to WindowsDataList
     */
    private class WindowData {
        private String startTime;
        private String endTime;
        private double rate;

        public WindowData(String startTime, double rate) {
            this.startTime = startTime;
            this.rate = rate;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        @Override
        public String toString() {
            return "Window: [" + startTime + (endTime != null ? " - " + endTime : "") + "], Rate: " + rate + " errors/minute";
        }
    }
}