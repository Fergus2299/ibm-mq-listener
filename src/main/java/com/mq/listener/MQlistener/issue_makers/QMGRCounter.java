package com.mq.listener.MQlistener.issue_makers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.models.Auth1ErrorDetails;
import com.mq.listener.MQlistener.models.AuthErrorDetails;
import com.mq.listener.MQlistener.models.ErrorDetails;
import com.mq.listener.MQlistener.models.ErrorSpike;
import com.mq.listener.MQlistener.models.UnknownObjectErrorDetails;
import com.mq.listener.MQlistener.utils.IssueAggregatorService;
import com.mq.listener.MQlistener.utils.JsonUtil;

// TODO: check it's the same QM every time, otherwise reset - Make sure to add QM to log files

@Component
public class QMGRCounter {
    private static final double ERRORS_PER_MINUTE_THRESHOLD = 10; // Threshold for errors per minute
    private static final long WINDOW_DURATION_MILLIS = 20 * 1000; // 10 seconds window
    private static final long MILLIS_IN_MINUTE = 60 * 1000; // 60 seconds * 1000 milliseconds/second
    
    
    // Active issues for each queue
    private static Map<String, ErrorSpike> issueObjectMap = new HashMap<>();
    // Temporary counts for each queue
    private static Map<String, ErrorDetails> tempCounts = new HashMap<>();
    // Start timestamps for each queue
    private static Map<String, Long> startTimestamps = new HashMap<>();
    // this will delete issues as they are closed
    Set<String> objectsWithIssues = new HashSet<>();
    
    // we wire the aggregator service so that it's there for adding errors
    @Autowired
    private IssueAggregatorService aggregatorService;
    
    public static void countType1AuthError(String userId, String appName, String channelName, String connName, String CSPUserId) {
    	startTimestamps.putIfAbsent("<QMGR - Auth>", System.currentTimeMillis());
    	ErrorDetails currentDetails = tempCounts.getOrDefault(
    			"<QMGR - Auth>", 
    			new Auth1ErrorDetails(0, userId, appName,channelName,connName,CSPUserId));
    	currentDetails.incrementCount();
    	tempCounts.put("<QMGR - Auth>", currentDetails);
    	}
    
    // Count 2035 - 2 errors for the given queue and error code
    public static void countType2AuthError(String userId, String appName, String queueName) {
        startTimestamps.putIfAbsent(queueName, System.currentTimeMillis());
        ErrorDetails currentDetails = tempCounts.getOrDefault(queueName, new AuthErrorDetails(0, userId, appName));
        currentDetails.incrementCount();
        tempCounts.put(queueName, currentDetails);
    }
    
    public static void countUnknownObjectError(String appName, String connName, String channelName, String queueName) {
    	startTimestamps.putIfAbsent("<QMGR - UnknownObject>", System.currentTimeMillis());
    	ErrorDetails currentDetails = tempCounts.getOrDefault(
    			"<QMGR - UnknownObject>", 
    			new UnknownObjectErrorDetails(0, appName, connName,channelName,queueName));
    	currentDetails.incrementCount();
    	tempCounts.put("<QMGR - UnknownObject>", currentDetails);
    	}
    

    // Evaluate error rates and reset counts at a fixed rate
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateAndResetCounts() {
    	System.out.println(startTimestamps.toString());
        long currentTimeMillis = System.currentTimeMillis();
        double rate;
        // TODO: ensure that the time interval is being evaluated correctly
        // Iterate over all queues with active issues and those in tempCounts
        Set<String> queuesToEvaluate = new HashSet<>(objectsWithIssues);
        queuesToEvaluate.addAll(tempCounts.keySet());
        for (String queue : queuesToEvaluate) {
        	ErrorDetails errorInfo;
        	// getting current info about the temp count
        	if (queue == "<QMGR - Auth>") {
    		 	errorInfo = tempCounts.getOrDefault(queue, new Auth1ErrorDetails(0, "", "", "", "", ""));
        	} else if (queue == "<QMGR - UnknownObject>") {
       		 	errorInfo = tempCounts.getOrDefault(queue, new UnknownObjectErrorDetails(0, "", "","",""));
        	} else {
        		errorInfo = tempCounts.getOrDefault(queue, new AuthErrorDetails(0, "", ""));
        	}
        	int count = errorInfo.getCount();
        	// time since the first issue
            long durationMillis = currentTimeMillis - startTimestamps.getOrDefault(queue, currentTimeMillis);
            rate = ((double) count / durationMillis) * MILLIS_IN_MINUTE;
            // Check the rate and handle the issues accordingly
            if (durationMillis > 0 && rate > ERRORS_PER_MINUTE_THRESHOLD) {
            	// get or create issue for this queue or whole queue manager
            	ErrorSpike issue;
            	if (queue == "<QMGR - Auth>") {
            		issue = issueObjectMap.getOrDefault(queue, new ErrorSpike("Too_Many_2035s","<QMGR>","<QMGR>"));
            		
            	} else if (queue == "<QMGR - UnknownObject>") {
            		issue = issueObjectMap.getOrDefault(queue, new ErrorSpike("Too_Many_2085s","<QMGR>","<QMGR>"));
            		
            	} else {
            		issue = issueObjectMap.getOrDefault(queue, new ErrorSpike("Too_Many_2035s","<Q>",queue));
            	}

                // adding the error info
                issue.addWindowData(errorInfo, rate);
                // we re-put into active issues
                issueObjectMap.put(queue, issue);
            } else {
            	// if the issue exists and has gone below the threshold then we can close the issue
                ErrorSpike issue = issueObjectMap.get(queue);
                if (issue != null) {
                    System.out.println("Deleting Issue");
                    issueObjectMap.remove(queue);
                }
            }
            
            if (issueObjectMap.containsKey(queue)) {
                objectsWithIssues.add(queue);
            } else {
                objectsWithIssues.remove(queue);
            }
        }
        
        
        // sending to the accumulator
        
        try {
            String jsonIssues = JsonUtil.toJson(issueObjectMap);
            aggregatorService.sendIssues(jsonIssues);
        } catch (Exception e) {
            System.err.println("Failed to send issues to aggregator: " + e.getMessage());
        }
        

        printCurrentIssues();
        // Clear only queues without active issues
        tempCounts.keySet().removeAll(objectsWithIssues);
        startTimestamps.keySet().removeAll(objectsWithIssues);
        
        // reset whole QMGR conns
//        authConnCount.reset();
    }
    // TODO: Implement a reset mechanism. Create a method or mechanism to reset counters outside of the threshold check (e.g., every hour or every day).
    
    // TODO: Integrate a notification mechanism. Introduce notifications, such as email alerts, when the error rate surpasses a threshold. This can work in conjunction with or as an alternative to creating issues.

    
    
    
 // Print current issues to the console
    private void printCurrentIssues() {
        System.out.println("---- CURRENT 2035 ISSUES ----");
        if (issueObjectMap.isEmpty()) {
            System.out.println("No issues detected.");
        } else {
            issueObjectMap.forEach((queue, issue) -> {
                System.out.println("Issue for: " + queue);
                issue.printIssueDetails();
                System.out.println("-------------------------------");  // Separate each issue with a line for clarity
            });
        }
        System.out.println("--------------------------");  // End line
    }

}