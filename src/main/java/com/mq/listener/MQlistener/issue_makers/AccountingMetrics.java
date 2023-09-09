package com.mq.listener.MQlistener.issue_makers;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.security.authentication.AnonymousAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.config.AppConfig;
import com.mq.listener.MQlistener.models.AccountingData;
import com.mq.listener.MQlistener.models.Issue.ConnectionPatternIssue;
import com.mq.listener.MQlistener.utils.ConsoleLogger;
import com.mq.listener.MQlistener.utils.IssueSender;

// we assume one user = one connecting app
// this isn't uniformed accross use cases of IBM MQ but is common because this 
// strategy means granular permissions and easier auditing.
@Component
public class AccountingMetrics {
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	// Config:
    private final AppConfig appConfig;
    int appConnectionsMax;
    float appConnectionOperationsConnections;
    float appConnectionOperationsMax;
    
    @Autowired
    private IssueSender sender;
    @Autowired
    public AccountingMetrics(AppConfig appConfig) {
        this.appConfig = appConfig;
    }
    
    public void printMaxConnections() {
        int maxConnections = appConfig.getConnections().getMax();
        System.out.println("Max connections: " + maxConnections);
    }
    

    
    
	// if the user is below this level of conns then their connection pattern is ignored
    // --- because we only penalise connection patterns which are frequent and therefore taking up 
    // a lot of resources
    
    // COR = shortening of connections operations ratio
//    @Value("${config.app.connectionOperationsRatio.connections}")
//    private int CORConnectionsThreshold;
    
//    @Value("${config.app.connectionOperationsRatio.max}")
//    private double CORThreshold;
    // the ratio of conns to put/gets where, if below this and connecting often then the app is ine-
    // fficient
    // time window length 
    private static final long WINDOW_DURATION_MILLIS = 10 * 1000;
   
    // Active issues for each userId
    private static Map<String, ConnectionPatternIssue> issueObjectMap = new HashMap<>();    // these maps hold temporarily the amount of connects and put/gets per user 
    private static Map<String, Integer> connectionCounts = new HashMap<>();
    private static Map<String, Integer> putGetCounts = new HashMap<>();
    
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
    public void evaluateAndResetCounts() throws Exception {
//    	System.out.println("getting username");
//    	String currentUserName = "";
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		System.out.println("auth : " + authentication);
//		if (!(authentication instanceof AnonymousAuthenticationToken)) {
//		    currentUserName = authentication.getName();
//		}
//		System.out.println("current user name: "+ currentUserName);
    	// loading config into function
    	// TODO: don't load ConnectionOperationsRatio twice
        appConnectionsMax = appConfig.getConnections().getMax();
        appConnectionOperationsConnections = (Integer) appConfig.getConnectionOperationsRatio().get("connections");
        // handling all number entries
        Number number = (Number) appConfig.getConnectionOperationsRatio().get("max");
        float appConnectionOperationsMax = number.floatValue();
    	System.out.println(
    "connThreshold: " 
    + appConnectionsMax 
    + ", CORConnectionsThreshold: " 
    + appConnectionOperationsConnections
    + ", CORThreshold: " 
	+ appConnectionOperationsMax);
    	// iterating through each user
        for (String userId : connectionCounts.keySet()) {
            int userConnectionCount = connectionCounts.getOrDefault(userId, 0);
            int userPutGetCount = putGetCounts.getOrDefault(userId, 0);
            System.out.println("User: " + userId + ", Connections: " + userConnectionCount + ", Put/Get Count: " + userPutGetCount);
            // checking if they've breached the connection threshold per minute
            double userRatio = userConnectionCount / (double) userPutGetCount;
            boolean shouldLogIssue = false;
            String errorMessage = "";
            ConnectionPatternIssue issue;
            
            // if app connects too much issue gets first priority, then we check for the ratio of conns
            if (userConnectionCount > appConnectionsMax) {
            	String windowDurationInSeconds = String.valueOf(WINDOW_DURATION_MILLIS / 1000);
                issue = issueObjectMap.getOrDefault(userId, new ConnectionPatternIssue(
                        userConnectionCount,
                        "Too many MQCONNs in time interval. Breached limit of " 
                                + appConnectionsMax
                                + " in the window of: " 
                                + windowDurationInSeconds
                                + ".",
                        userPutGetCount,
                        userId
                    ));
                LocalTime endTimeFormatted = LocalTime.now();
                LocalTime startTimeFormatted = endTimeFormatted.minusSeconds(WINDOW_DURATION_MILLIS / 1000);
                
                String combinedTime = startTimeFormatted.format(timeFormatter) + " - " + endTimeFormatted.format(timeFormatter);
                Map<String, String> issueDetails = new HashMap<>();
                issueDetails.put("conns", String.valueOf(userConnectionCount));
                issueDetails.put("putGetCount", String.valueOf(userPutGetCount));
                issueDetails.put("userRatio", String.valueOf(userRatio));
                
                issue.addWindowData(issueDetails, combinedTime);
                System.out.println("sending app issue");
                sender.sendIssue(issue);
                issueObjectMap.put(userId, issue);
                
            } else if (
            		userConnectionCount > appConnectionOperationsConnections && userRatio >= appConnectionOperationsMax) {
                String windowDurationInSeconds = String.valueOf(WINDOW_DURATION_MILLIS / 1000);
                
                issue = issueObjectMap.getOrDefault(userId, new ConnectionPatternIssue(
                        userConnectionCount,
                        "Ratio of MQCONNS to GETS/PUTS is above " 
                                + appConnectionOperationsMax
                                + " too high " 
                                + " in the configured interval of "
                                + windowDurationInSeconds
                                + ".",
                        userPutGetCount,
                        userId
                    ));
                LocalTime endTimeFormatted = LocalTime.now();
                LocalTime startTimeFormatted = endTimeFormatted.minusSeconds(WINDOW_DURATION_MILLIS / 1000);
                String combinedTime = startTimeFormatted.format(timeFormatter) + " - " + endTimeFormatted.format(timeFormatter);

                Map<String, String> issueDetails = new HashMap<>();
                issueDetails.put("conns", String.valueOf(userConnectionCount));
                issueDetails.put("putGetCount", String.valueOf(userPutGetCount));
                issueDetails.put("userRatio", String.valueOf(userRatio));
                
                issue.addWindowData(issueDetails, combinedTime);
                System.out.println("sending app issue");
                // sending created or updated issue to frontend
                sender.sendIssue(issue);
                issueObjectMap.put(userId, issue);
            }
            System.out.println("User: " + userId + ", Connections: " + userConnectionCount + ", userRatio: " + userRatio);

        }
        // Reset the counts for the next window
        connectionCounts.clear();
        putGetCounts.clear();
    }
}