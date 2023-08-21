package com.mq.listener.MQlistener.issue_makers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.models.Auth1ErrorDetails;
import com.mq.listener.MQlistener.models.AuthErrorDetails;
import com.mq.listener.MQlistener.models.TooMany2035sIssue;

// TODO: check it's the same QM every time, otherwise reset - Make sure to add QM to log files

@Component
public class AuthCounter {
    private static final double ERRORS_PER_MINUTE_THRESHOLD = 10; // Threshold for errors per minute
    private static final long WINDOW_DURATION_MILLIS = 20 * 1000; // 10 seconds window
    private static final long MILLIS_IN_MINUTE = 60 * 1000; // 60 seconds * 1000 milliseconds/second
    
    // Counting the total 2035 - 1 for the whole QM
    private static Auth1ErrorDetails authConnCount = new Auth1ErrorDetails(0, "", "", "", "", "");
    // the count is the key and the details we want for that window are the vals
    private static Map<String, TooMany2035sIssue> activeType1Issues = new HashMap<>();
    
    // Active issues for each queue
    private static Map<String, TooMany2035sIssue> activeType2Issues = new HashMap<>();
    // Temporary counts for each queue
    private static Map<String, AuthErrorDetails> tempCounts = new HashMap<>();
    // Start timestamps for each queue
    private static Map<String, Long> startTimestamps = new HashMap<>();
    // this will delete issues as they are closed
    Set<String> queuesWithIssues = new HashSet<>();
    
    public static void countType1Error(String userId, String appName, String channelName, String connName, String CSPUserId) {
    	// default the count to zero for this window and input the vals
//    	Auth1ErrorDetails currentDetails = authConnCount.getOrDefault("QMGR", new Auth1ErrorDetails(0, userId, appName, channelName, connName, CSPUserId));
    	authConnCount.incrementCount();
    	authConnCount.setUserId(userId);
    	authConnCount.setAppName(appName);
    	authConnCount.setChannelName(channelName);
    	authConnCount.setConnName(connName);
    	authConnCount.setCSPUserId(CSPUserId);
    	}
    
    // Count 2035 - 2 errors for the given queue and error code
    public static void countType2Error(String userId, String appName, String queueName) {
        startTimestamps.putIfAbsent(queueName, System.currentTimeMillis());
        AuthErrorDetails currentDetails = tempCounts.getOrDefault(queueName, new AuthErrorDetails(0, userId, appName));
//        tempCounts.put(queueName, tempCounts.getOrDefault(queueName, 0) + 1);
        currentDetails.setCount(currentDetails.getCount() + 1);
        tempCounts.put(queueName, currentDetails);
    }
    
    

    // Evaluate error rates and reset counts at a fixed rate
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateAndResetCounts() {
        long currentTimeMillis = System.currentTimeMillis();
        // check num of type 1
        double rate = ((double) authConnCount.getCount() / WINDOW_DURATION_MILLIS) * MILLIS_IN_MINUTE;
        
        if (rate >= ERRORS_PER_MINUTE_THRESHOLD) {
        	System.out.println("too many type 1's");
        	// get or default issue and add window data
        	TooMany2035sIssue issue = activeType1Issues.getOrDefault("QMGR", new TooMany2035sIssue("QMGR", "_"));
        	issue.addWindowData(authConnCount, rate);
        	// put back in
        	activeType1Issues.put("QMGR", issue);
        } 
        if (activeType1Issues.containsKey("QMGR") && rate < ERRORS_PER_MINUTE_THRESHOLD) { // if issue already exists but now below rate
        	TooMany2035sIssue issue = activeType1Issues.get("QMGR");
        	// close the issue and remove from active issues
        	System.out.println("Deleting Type 1 Issue");
            issue.closeIssue();
        	activeType1Issues.remove("QMGR");
        }

        // Iterate over all queues with active issues and get counts
        Set<String> queuesToEvaluate = new HashSet<>(queuesWithIssues);
        queuesToEvaluate.addAll(tempCounts.keySet());
        
        for (String queue : queuesToEvaluate) {
        	// getting current info about the temp count
        	AuthErrorDetails errorInfo = tempCounts.getOrDefault(queue, new AuthErrorDetails(0, "", ""));
        	int count = errorInfo.getCount();
            long durationMillis = currentTimeMillis - startTimestamps.getOrDefault(queue, currentTimeMillis);
            rate = ((double) count / durationMillis) * MILLIS_IN_MINUTE;
            // Check the rate and handle the issues accordingly
            if (durationMillis > 0 && rate > ERRORS_PER_MINUTE_THRESHOLD) {
            	// will get the active issue for this queue or will create one
                TooMany2035sIssue issue = activeType2Issues.getOrDefault(queue, new TooMany2035sIssue("queue",queue));
                // adding the error info
                issue.addWindowData(errorInfo, rate);
                // we re-put into active issues
                activeType2Issues.put(queue, issue);
            } else {
            	// if the issue exists and has gone below the threshold then we can close the issue
                TooMany2035sIssue issue = activeType2Issues.get(queue);
                if (issue != null) {
                    System.out.println("Deleting Issue");
                    activeType2Issues.remove(queue);
                }
            }
            
            if (activeType2Issues.containsKey(queue)) {
                queuesWithIssues.add(queue);
            } else {
                queuesWithIssues.remove(queue);
            }
        }
        

        printCurrentIssues();
        // Clear only queues without active issues
        tempCounts.keySet().removeAll(queuesWithIssues);
        startTimestamps.keySet().removeAll(queuesWithIssues);
        
        // reset whole QMGR conns
        authConnCount.reset();
    }
    // TODO: Implement a reset mechanism. Create a method or mechanism to reset counters outside of the threshold check (e.g., every hour or every day).
    
    // TODO: Integrate a notification mechanism. Introduce notifications, such as email alerts, when the error rate surpasses a threshold. This can work in conjunction with or as an alternative to creating issues.

    
    
    
 // Print current issues to the console
    private void printCurrentIssues() {
        System.out.println("---- CURRENT 2035 ISSUES ----");
        if (activeType1Issues.isEmpty() && activeType2Issues.isEmpty()) {
            System.out.println("No issues detected.");
        } else {
        	activeType1Issues.forEach((QM, issue) -> {
                System.out.println("Issue for QM: " + QM);
                issue.printIssueDetails();
                System.out.println("-------------------------------");  // Separate each issue with a line for clarity
            });
            activeType2Issues.forEach((queue, issue) -> {
                System.out.println("Issue for queue: " + queue);
                issue.printIssueDetails();
                System.out.println("-------------------------------");  // Separate each issue with a line for clarity
            });
        }
        System.out.println("--------------------------");  // End line
    }

    public static Map<String, TooMany2035sIssue> getActiveIssues() {
        return activeType2Issues;
    }
}