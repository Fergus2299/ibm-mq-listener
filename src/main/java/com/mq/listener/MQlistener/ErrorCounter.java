package com.mq.listener.MQlistener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// TODO: check it's the same QM every time, otherwise reset - Make sure to add QM to log files

@Component
public class ErrorCounter {
    private static final double ERRORS_PER_MINUTE_THRESHOLD = 5; // Threshold for errors per minute
    private static final long WINDOW_DURATION_MILLIS = 10 * 1000; // 10 seconds window
    private static final long MILLIS_IN_MINUTE = 60 * 1000; // 60 seconds * 1000 milliseconds/second

    // Active issues for each queue
    private static Map<String, TooMany2035sIssue> activeIssues = new HashMap<>();
    // Temporary counts for each queue
    private static Map<String, Integer> tempCounts = new HashMap<>();
    // Start timestamps for each queue
    private static Map<String, Long> startTimestamps = new HashMap<>();
    // this will delete issues as they are closed
    Set<String> queuesWithIssues = new HashSet<>();

    // Count errors for the given queue and error code
    public static void countError(String queueName, int errorCode) {
        if (errorCode != 2035) return;
        // assuming that queue manager is staying the same during this process and names are unique enough
        // that they aren't shared by different QMs
        startTimestamps.putIfAbsent(queueName, System.currentTimeMillis());
        tempCounts.put(queueName, tempCounts.getOrDefault(queueName, 0) + 1);
        
//        System.out.println("Updated tempCounts: " + tempCounts);
    }

    // Evaluate error rates and reset counts at a fixed rate
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateAndResetCounts() {
        long currentTimeMillis = System.currentTimeMillis();
        
        // Iterate over all queues with active issues and get counts
        Set<String> queuesToEvaluate = new HashSet<>(queuesWithIssues);
        queuesToEvaluate.addAll(tempCounts.keySet());
        
        for (String queue : queuesToEvaluate) {
            int count = tempCounts.getOrDefault(queue, 0);
            long durationMillis = currentTimeMillis - startTimestamps.getOrDefault(queue, currentTimeMillis);
            double rate = ((double) count / durationMillis) * MILLIS_IN_MINUTE;
            System.out.format("durationMillis: %d%n",durationMillis);
            System.out.format("rate: %f%n",rate);
            // Check the rate and handle the issues accordingly
            if (durationMillis > 0 && rate > ERRORS_PER_MINUTE_THRESHOLD) {
            	// will get the active issue for this queue or will create one
                TooMany2035sIssue issue = activeIssues.getOrDefault(queue, new TooMany2035sIssue(queue));
                issue.addWindowData(issue.formatNow(), rate);
                // we re-put into active issues
                activeIssues.put(queue, issue);
            } else {
            	// if the issue exists and has gone below the threshold then we can close the issue
                TooMany2035sIssue issue = activeIssues.get(queue);
                if (issue != null && issue.isIssueActive()) {
                    issue.closeLatestWindow(issue.formatNow());
                    System.out.println("Deleting Issue");
                    issue.closeIssue();
                    activeIssues.remove(queue);
                }
            }
            
            if (activeIssues.containsKey(queue)) {
                queuesWithIssues.add(queue);
            } else {
                queuesWithIssues.remove(queue);
            }
        }
        

        printCurrentIssues();
        // Clear only queues without active issues
        tempCounts.keySet().removeAll(queuesWithIssues);
        startTimestamps.keySet().removeAll(queuesWithIssues);
    }
    // TODO: Implement a reset mechanism. Create a method or mechanism to reset counters outside of the threshold check (e.g., every hour or every day).
    
    // TODO: Integrate a notification mechanism. Introduce notifications, such as email alerts, when the error rate surpasses a threshold. This can work in conjunction with or as an alternative to creating issues.

    
    
    
 // Print current issues to the console
    private void printCurrentIssues() {
        System.out.println("---- CURRENT 2035 ISSUES ----");
        if (activeIssues.isEmpty()) {
            System.out.println("No issues detected.");
        } else {
            activeIssues.forEach((queue, issue) -> {
                System.out.println("Issue for queue: " + queue);
                System.out.println(issue.getIssueReport());
                System.out.println("-------------------------------");  // Separate each issue with a line for clarity
            });
        }
        System.out.println("--------------------------");  // End line
    }

    // TODOs for future enhancements
    // ...

    // Getter for the active issues
    public static Map<String, TooMany2035sIssue> getActiveIssues() {
        return activeIssues;
    }
}