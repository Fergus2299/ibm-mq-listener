package com.mq.listener.MQlistener;

import java.util.HashMap;
import java.util.Map;

public class ErrorCounter {
    private static final int THRESHOLD = 5; // hard-coded threshold for 2035 errors
    private static final double ERRORS_PER_MINUTE_THRESHOLD = 5; // For demonstration

    // Store active issues for queues
    private static Map<String, TooMany2035sIssue> activeIssues = new HashMap<>();
    
    // Temporary storage for 2035 errors' count for each queue, until the threshold is reached
    private static Map<String, Integer> tempCounts = new HashMap<>();
    

 // A map to track the start timestamp for each queue
    private static Map<String, Long> startTimestamps = new HashMap<>();

    public static void countError(String queueName, int errorCode) {
        if (errorCode != 2035) return;

        // Increment the temp count
        tempCounts.put(queueName, tempCounts.getOrDefault(queueName, 0) + 1);

        // If this is the first error for this queue, record the timestamp
        startTimestamps.putIfAbsent(queueName, System.currentTimeMillis());

        long startTime = startTimestamps.get(queueName);
        long durationMillis = System.currentTimeMillis() - startTime; // Duration in milliseconds

        // Calculate the errors per minute rate
        double rate = (tempCounts.get(queueName) * 60000.0) / durationMillis;

        if (rate >= ERRORS_PER_MINUTE_THRESHOLD) {
            TooMany2035sIssue issue = activeIssues.getOrDefault(queueName, new TooMany2035sIssue(queueName));
            issue.incrementRequestCount();
            activeIssues.put(queueName, issue);

            // Reset counters for this queue to start a new analysis period
            startTimestamps.remove(queueName);
            tempCounts.remove(queueName);
            
            System.out.println("Issue created for queue: " + queueName + ". Error rate: " + rate + " errors/minute.");
        } else {
            System.out.println("Temporary count incremented for queue: " + queueName + ". Current rate: " + rate + " errors/minute.");
        }
        // TODO: Implement windowed analysis. Instead of a sliding window, consider using fixed windows for a structured analysis.
        
        // TODO: Implement a reset mechanism. Create a method or mechanism to reset counters outside of the threshold check (e.g., every hour or every day).
        
        // TODO: Integrate a notification mechanism. Introduce notifications, such as email alerts, when the error rate surpasses a threshold. This can work in conjunction with or as an alternative to creating issues.
    }

    // Getter to retrieve active issues
    public static Map<String, TooMany2035sIssue> getActiveIssues() {
        return activeIssues;
    }
    public static void resetCounters(String queueName) {
        tempCounts.remove(queueName);
        // Optionally, you might want to also remove active issues
        // activeIssues.remove(queueName);
    }
    public static void resetAllCounters() {
        tempCounts.clear();
        // Optionally, you might want to also clear all active issues
        // activeIssues.clear();
    }
}