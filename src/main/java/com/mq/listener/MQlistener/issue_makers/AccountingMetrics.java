package com.mq.listener.MQlistener.issue_makers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.models.AccountingData;
import com.mq.listener.MQlistener.models.Issue.ConnectionPatternIssue;
import com.mq.listener.MQlistener.utils.ConsoleLogger;
import com.mq.listener.MQlistener.utils.IssueAggregatorService;

// we assume one user = one connecting app
// this isn't uniformed accross use cases of IBM MQ but is common because this 
// strategy means granular permissions and easier auditing.
@Component
public class AccountingMetrics {
	
	// hardcoded threshold of number of conns per minute
    private static final int CONNECTION_THRESHOLD = 100; 
    
	// if the user is below this level of conns then their connection pattern is ignored
    // --- because we only penalise connection patterns which are frequent and therefore taking up 
    // a lot of resources
    private static final int RATIO_CONNECTION_THRESHOLD = 5;
    // the ratio of conns to put/gets where, if below this and connecting often then the app is ine-
    // fficient
    private static final double RATIO_THRESHOLD = 0.80;
    // time window length 
    private static final long WINDOW_DURATION_MILLIS = 10 * 1000;
   
    // Active issues for each userId
    private static Map<String, ConnectionPatternIssue> issueObjectMap = new HashMap<>();    // these maps hold temporarily the amount of connects and put/gets per user 
    private static Map<String, Integer> connectionCounts = new HashMap<>();
    private static Map<String, Integer> putGetCounts = new HashMap<>();
    
    // we wire the aggregator service so that it's there for adding errors
    @Autowired
    private IssueAggregatorService aggregatorService;
    
    // TODO: synchronized allows for no one of these to be running at one time and therefore is more thread safe
    
    public static synchronized void addMessage(AccountingData data) {
        String userId = data.getUserIdentifier();
        // Update connection count
        connectionCounts.put(userId, connectionCounts.getOrDefault(userId, 0) + 1);
        
        // Update put/get count - which includes failed attempts because sometimes an app
        // is just checking a queue
        int currentPutGetCount = 
        		data.getPuts()
        		+ data.getGets()
        		+ data.getPut1s()
        		+ data.getPutsFailed()
        		+ data.getGetsFailed()
        		+ data.getPut1sFailed();
        putGetCounts.put(userId, putGetCounts.getOrDefault(userId, 0) + currentPutGetCount);
    }
    
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateAndResetCounts() {
//    	System.out.println("Doing scheduled function");
//        System.out.println("Connections: " + connectionCounts);
//        System.out.println("putGetCounts: " + putGetCounts);
    	
    	
    	// iterating through each user
        for (String userId : connectionCounts.keySet()) {
            int userConnectionCount = connectionCounts.getOrDefault(userId, 0);
            int userPutGetCount = putGetCounts.getOrDefault(userId, 0);
            System.out.println("User: " + userId + ", Connections: " + userConnectionCount + ", Put/Get Count: " + userPutGetCount);
            // checking if they've breached the connection threshold per minute
            
            if (userConnectionCount > CONNECTION_THRESHOLD && !issueObjectMap.containsKey(userId)) {
//                System.out.println("Warning: User " + userId + " has breached the connection threshold with " + userConnectionCount + " connections!");
            	String windowDurationInSeconds = String.valueOf(WINDOW_DURATION_MILLIS / 1000);
            	ConnectionPatternIssue error = new ConnectionPatternIssue(
            			userConnectionCount,
            			"Too many MQCONNs in time interval. Breached limit of " 
            					+ CONNECTION_THRESHOLD 
            					+ " in the window of: " 
            					+ windowDurationInSeconds
            					+ ".",
            			userPutGetCount, 
            			userId);
            	issueObjectMap.putIfAbsent(userId, error);
            } // TODO: add window data here
            
            // checking if they've got inefficient connection patterns
            // e.g., if an app is connecting more than 20 times per min and puts/gets less than 50 times
            // this could be seen as inefficient
            if (userConnectionCount > RATIO_CONNECTION_THRESHOLD) {
                double userRatio = userConnectionCount/ (double) userPutGetCount ;
                System.out.println(userId + "UserRatio: " + userRatio);
                if (userRatio >= RATIO_THRESHOLD && !issueObjectMap.containsKey(userId)) {
//                    System.out.println("Warning: User " + userId + " has breached the ratio threshold with a ratio of " + userRatio + "!");
                	String windowDurationInSeconds = String.valueOf(WINDOW_DURATION_MILLIS / 1000);
                	ConnectionPatternIssue error = new ConnectionPatternIssue(
                			userConnectionCount,
                			"Ratio of MQCONNS to GETS/PUTS is above " 
	                			+ RATIO_THRESHOLD 
	                			+ " too high " 
	                			+ " in the configured interval of "
	                			+ windowDurationInSeconds
	                			+ ".",
                			userPutGetCount, 
                			userId);
                	issueObjectMap.putIfAbsent(userId, error);
                }
            }
        }
        // Reset the counts for the next window
        connectionCounts.clear();
        putGetCounts.clear();
        // print the current issues
        ConsoleLogger.printQueueCurrentIssues(issueObjectMap, "Apps");
        
        // send new version of issues to the aggregator
        try {
        	aggregatorService.sendIssues("ApplicationConfigurationIssues", issueObjectMap);
        } catch (Exception e) {
            System.err.println("Failed to send issues to aggregator: " + e.getMessage());
        }
    }
}