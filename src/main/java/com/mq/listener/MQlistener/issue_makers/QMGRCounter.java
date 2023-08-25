package com.mq.listener.MQlistener.issue_makers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.models.Errors.Auth1ErrorDetails;
import com.mq.listener.MQlistener.models.Errors.AuthErrorDetails;
import com.mq.listener.MQlistener.models.Errors.ErrorDetails;
import com.mq.listener.MQlistener.models.Errors.UnknownObjectErrorDetails;
import com.mq.listener.MQlistener.models.Issue.ErrorSpike;
import com.mq.listener.MQlistener.utils.ConsoleLogger;
import com.mq.listener.MQlistener.utils.IssueAggregatorService;
import com.mq.listener.MQlistener.utils.JsonUtil;

@Component
public class QMGRCounter {
    private static final double ERRORS_PER_MINUTE_THRESHOLD = 10; // Threshold for errors per minute
    private static final long WINDOW_DURATION_MILLIS = 20 * 1000; // 10 seconds window
    private static final long MILLIS_IN_MINUTE = 60 * 1000; // 60 seconds * 1000 milliseconds/second
    
    
    // Active issues for each queue
    private static Map<String, ErrorSpike> issueObjectMap = new HashMap<>();
    // Temporary counts for each queue
    private static Map<String, ErrorDetails> tempCounts = new HashMap<>();
    // this will delete issues as they are closed
    Set<String> objectsWithIssues = new HashSet<>();
    
    // we wire the aggregator service so that it's there for adding errors
    @Autowired
    private IssueAggregatorService aggregatorService;
    
    public static void countType1AuthError(String userId, String appName, String channelName, String connName, String CSPUserId) {
//    	startTimestamps.putIfAbsent("<QMGR - Auth>", System.currentTimeMillis());
    	ErrorDetails currentDetails = tempCounts.getOrDefault(
    			"<QMGR - Auth>", 
    			new Auth1ErrorDetails(0, userId, appName,channelName,connName,CSPUserId));
    	currentDetails.incrementCount();
    	tempCounts.put("<QMGR - Auth>", currentDetails);
    	}
    
    // Count 2035 - 2 errors for the given queue and error code
    public static void countType2AuthError(String userId, String appName, String queueName) {
//        startTimestamps.putIfAbsent(queueName, System.currentTimeMillis());
        ErrorDetails currentDetails = tempCounts.getOrDefault(queueName, new AuthErrorDetails(0, userId, appName));
        currentDetails.incrementCount();
        tempCounts.put(queueName, currentDetails);
    }
    
    public static void countUnknownObjectError(String appName, String connName, String channelName, String queueName) {
//    	startTimestamps.putIfAbsent("<QMGR - UnknownObject>", System.currentTimeMillis());
    	ErrorDetails currentDetails = tempCounts.getOrDefault(
    			"<QMGR - UnknownObject>", 
    			new UnknownObjectErrorDetails(0, appName, connName,channelName,queueName));
    	currentDetails.incrementCount();
    	tempCounts.put("<QMGR - UnknownObject>", currentDetails);
    	}
    

    // Evaluate error rates and reset counts at a fixed rate
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateAndResetCounts() {
        double rate;
        // TODO: ensure that the time interval is being evaluated correctly
        // Iterate over all queues with active issues and those in tempCounts
        Set<String> queuesToEvaluate = new HashSet<>(objectsWithIssues);
        queuesToEvaluate.addAll(tempCounts.keySet());
        for (String mqObject : queuesToEvaluate) {
        	ErrorDetails errorInfo;
        	// getting current info about the temp count
        	if (mqObject == "<QMGR - Auth>") {
    		 	errorInfo = tempCounts.getOrDefault(mqObject, new Auth1ErrorDetails(0, "", "", "", "", ""));
        	} else if (mqObject == "<QMGR - UnknownObject>") {
       		 	errorInfo = tempCounts.getOrDefault(mqObject, new UnknownObjectErrorDetails(0, "", "","",""));
        	} else {
        		errorInfo = tempCounts.getOrDefault(mqObject, new AuthErrorDetails(0, "", ""));
        	}
        	int count = errorInfo.getCount();
        	// time since the first issue
//            long durationMillis = currentTimeMillis - startTimestamps.getOrDefault(queue, currentTimeMillis);
            rate = ((double) count / WINDOW_DURATION_MILLIS) * MILLIS_IN_MINUTE;
            // Check the rate and handle the issues accordingly
            if (rate > ERRORS_PER_MINUTE_THRESHOLD) {
            	// get or create issue for this queue or whole queue manager
            	ErrorSpike issue;
            	if (mqObject == "<QMGR - Auth>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<QMGR>","<QMGR>"));
            		
            	} else if (mqObject == "<QMGR - UnknownObject>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2085s","<QMGR>","<QMGR>"));
            		
            	} else {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<Q>",mqObject));
            	}

                // adding the error info
                issue.addWindowData(errorInfo, rate);
                // we re-put into active issues
                issueObjectMap.put(mqObject, issue);
            } else {
            	// if the issue exists and has gone below the threshold then we can close the issue
                ErrorSpike issue = issueObjectMap.get(mqObject);
                if (issue != null) {
                    System.out.println("Deleting Issue");
                    issueObjectMap.remove(mqObject);
                }
            }
            
            if (issueObjectMap.containsKey(mqObject)) {
                objectsWithIssues.add(mqObject);
            } else {
                objectsWithIssues.remove(mqObject);
            }
        }
        
        
        // sending to the accumulator
        try {
        	aggregatorService.sendIssues("ErrorSpikeIssues", issueObjectMap);
        } catch (Exception e) {
            System.err.println("Failed to send issues to aggregator: " + e.getMessage());
        }
        

        ConsoleLogger.printQueueCurrentIssues(issueObjectMap, "Error Rates");
        // Clear only queues without active issues
        tempCounts.keySet().removeAll(objectsWithIssues);

    }
}