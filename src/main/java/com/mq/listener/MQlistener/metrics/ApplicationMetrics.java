package com.mq.listener.MQlistener.metrics;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.config.Config.QMConfig;
import com.mq.listener.MQlistener.models.AccountingData;
import com.mq.listener.MQlistener.models.Issue.ConnectionPatternIssue;
import com.mq.listener.MQlistener.processors.QMGRProcessor;
import com.mq.listener.MQlistener.utils.IssueSender;

// we assume one user = one connecting app
// this isn't uniformed accross use cases of IBM MQ but is common because this 
// strategy means granular permissions and easier auditing.
@Component
public class ApplicationMetrics {
    private static final Logger log = LoggerFactory.getLogger(ApplicationMetrics.class);

	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
	// Config:
	private final ConfigManager configManager;
    int appConnectionsMax;
    float appConnectionOperationsConnections;
    float appConnectionOperationsMax;
    
    @Autowired
    private IssueSender sender;
    @Autowired
    public ApplicationMetrics(ConfigManager configManager) {
    	this.configManager = configManager;
    }
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;

    // time window length 60 seconds
    private static final long WINDOW_DURATION_MILLIS = 60 * 1000;
   
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
    	log.info("Checking application statistics");
    	// load specific queue manger settings
    	QMConfig queueManagerConfig = 
    	configManager
    	.getConfig()
    	.getQms()
    	.getOrDefault(
    			qMgrName, 
    			configManager.getConfig().getQms().get("<DEFAULT>"));
    	
    	// getting max MQCONNs per application
        appConnectionsMax = 
        queueManagerConfig
        .getApp()
        .getConnections()
        .getMax();
        
        // getting max conns to consider MQCONN:MQOPERATIONS ratio
        appConnectionOperationsConnections = 
        queueManagerConfig
        .getApp()
        .getConnectionOperationsRatio()
        .getConnections();
        
        
        // getting MQCONN:MQOPERATIONS ratio threshold for issue to be created
        Number number = 
        (Number) queueManagerConfig
        .getApp()
        .getConnectionOperationsRatio()
        .getMax();
        float appConnectionOperationsMax = number.floatValue();
        
        
        log.info(
    "Apps Configuration: "
    + "connThreshold: " 
    + appConnectionsMax 
    + ", CORConnectionsThreshold: " 
    + appConnectionOperationsConnections
    + ", CORThreshold: " 
	+ appConnectionOperationsMax);
    	// iterating through each user
        for (String userId : connectionCounts.keySet()) {
            int userConnectionCount = connectionCounts.getOrDefault(userId, 0);
            int userPutGetCount = putGetCounts.getOrDefault(userId, 0);
            log.info("User: " + userId + ", Connections: " + userConnectionCount + ", Put/Get Count: " + userPutGetCount);
            // checking if they've breached the connection threshold per minute
            double userRatio = userConnectionCount / (double) userPutGetCount;
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
                // sending created or updated issue to frontend
                sender.sendIssue(issue);
                issueObjectMap.put(userId, issue);
            }
//            System.out.println("User: " + userId + ", Connections: " + userConnectionCount + ", userRatio: " + userRatio);

        }
        // Reset the counts for the next window
        connectionCounts.clear();
        putGetCounts.clear();
    }
}