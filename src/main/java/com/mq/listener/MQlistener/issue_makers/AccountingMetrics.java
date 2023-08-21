//package com.mq.listener.MQlistener.issue_makers;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.LinkedList;
//import java.util.Map;
//import java.util.Queue;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import com.mq.listener.MQlistener.models.AccountingData;
//import com.mq.listener.MQlistener.models.ConnectionPatternIssue;
//
//
//// we assume one user = one connecting app
//// this isn't uniformed accross use cases of IBM MQ but is common because this 
//// strategy means granular permissions and easier auditing.
//@Component
//public class AccountingMetrics {
//    
//    private static final int CONNECTION_THRESHOLD = 100; // example value, adjust as necessary
//    private static final int RATIO_CONNECTION_THRESHOLD = 5; // example value, adjust as necessary
//    private static final double RATIO_THRESHOLD = 0.80; // example value, adjust as necessary
//    private static final long WINDOW_DURATION_MILLIS = 10 * 1000;
//   
//    // Active issues for each userId
//    private static Map<String, ConnectionPatternIssue> issuesMap = new HashMap<>();    // these maps hold temporarily the amount of connects and put/gets per user 
//    private static Map<String, Integer> connectionCounts = new HashMap<>();
//    private static Map<String, Integer> putGetCounts = new HashMap<>();
//    // synchronized allows for no one of these to be running at one time and therefore is more thread safe
//    
//    // TODO: this function may not need to exist
//    public static synchronized void addMessage(AccountingData data) {
//        String userId = data.getUserIdentifier();
//
//        // Update connection count
//        connectionCounts.put(userId, connectionCounts.getOrDefault(userId, 0) + 1);
//
//        // Update put/get count - which includes failed attempts because sometimes an app
//        // is just checking a queue
//        int currentPutGetCount = data.getPuts()
//        		+ data.getGets() 
//        		+ data.getPut1s() 
//        		+ data.getPutsFailed() 
//        		+ data.getGetsFailed() 
//        		+ data.getPut1sFailed();
//        putGetCounts.put(userId, putGetCounts.getOrDefault(userId, 0) + currentPutGetCount);
//
//    }
//    
//    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
//    public void evaluateAndResetCounts() {
////    	System.out.println("Doing scheduled function");
////        System.out.println("Connections: " + connectionCounts);
////        System.out.println("putGetCounts: " + putGetCounts);
//    	// iterating through each user
//        for (String userId : connectionCounts.keySet()) {
//            int userConnectionCount = connectionCounts.getOrDefault(userId, 0);
//            int userPutGetCount = putGetCounts.getOrDefault(userId, 0);
//            System.out.println("User: " + userId + ", Connections: " + userConnectionCount + ", Put/Get Count: " + userPutGetCount);
//            // checking if they've breached the connection threshold per minute
//            if (userConnectionCount > CONNECTION_THRESHOLD && !issuesMap.containsKey(userId)) {
////                System.out.println("Warning: User " + userId + " has breached the connection threshold with " + userConnectionCount + " connections!");
//            	ConnectionPatternIssue error = new ConnectionPatternIssue(userConnectionCount, userPutGetCount, userId);
//                addIssue(userId, error);
//            }
//            
//            
//            // checking if they've got inefficient connection patterns
//            // e.g., if an app is connecting more than 20 times per min and puts/gets less than 50 times
//            // this could be seen as inefficient
//            if (userConnectionCount > RATIO_CONNECTION_THRESHOLD) {
//                double userRatio = userConnectionCount/ (double) userPutGetCount ;
//                System.out.println(userId + "UserRatio: " + userRatio);
//                if (userRatio >= RATIO_THRESHOLD && !issuesMap.containsKey(userId)) {
////                    System.out.println("Warning: User " + userId + " has breached the ratio threshold with a ratio of " + userRatio + "!");
//                	ConnectionPatternIssue error = new ConnectionPatternIssue(userConnectionCount, userPutGetCount, userId);
//                    addIssue(userId, error);
//                }
//            }
//        }
//
//        // Reset the counts for the next window
//        connectionCounts.clear();
//        putGetCounts.clear();
//        // print the current issues
//        generateIssueReport();
//    }
//    
//    private static void addIssue(String app, ConnectionPatternIssue issue) {
//        issuesMap.putIfAbsent(app, issue);
//    }
//    private void generateIssueReport() {
//        System.out.println("------- CONNECTION PATTERNS ISSUES REPORT -------");
//        if(issuesMap.isEmpty()) {
//            System.out.println("No issues detected.");
//        } else {
//            issuesMap.forEach((appName, issue) -> {
//                System.out.println("Issue for app: " + appName);
//                System.out.println(issue.getIssueReport());
//                System.out.println("-------------------------------");  // Separate each issue with a line for clarity
//            });
//        }
//        System.out.println("----------------------------");
//    }
//
//
//
//}