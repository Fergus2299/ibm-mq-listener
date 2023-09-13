package com.mq.listener.MQlistener.issue_makers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.mq.listener.MQlistener.config.QueueConfig;
import com.mq.listener.MQlistener.config.QueueManagerConfig;
import com.mq.listener.MQlistener.models.Errors.Auth1ErrorDetails;
import com.mq.listener.MQlistener.models.Errors.AuthErrorDetails;
import com.mq.listener.MQlistener.models.Errors.ErrorDetails;
import com.mq.listener.MQlistener.models.Errors.UnknownObjectErrorDetails;
import com.mq.listener.MQlistener.models.Issue.ErrorSpike;
import com.mq.listener.MQlistener.newConfig.Config.QMConfig;
import com.mq.listener.MQlistener.newConfig.ConfigManager;
import com.mq.listener.MQlistener.utils.ConsoleLogger;
import com.mq.listener.MQlistener.utils.IssueSender;

@Component
public class QMGRCounter {
    private static final Logger log = LoggerFactory.getLogger(QMGRCounter.class);
    

    private final ConfigManager configManager;
    int queueManagerThreshold;
    int queueThreshold;
    
    @Autowired
    private IssueSender sender;
    
    @Autowired
    public QMGRCounter(ConfigManager configManager) {
        this.configManager = configManager;

    }
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;
    
    
    
    // using value injection to get the threshold for errors per minute
//    @Value("${config.queue-manager.errors.max}")
//    private double queueManagerThreshold;
//    
//    @Value("${config.queue.errors.max}")
//    private double queueThreshold;
//    private static final double ERRORS_PER_MINUTE_THRESHOLD = 10; // Threshold for errors per minute
    private static final long WINDOW_DURATION_MILLIS = 20 * 1000; // 10 seconds window
    private static final long MILLIS_IN_MINUTE = 60 * 1000; // 60 seconds * 1000 milliseconds/second
    
    // Active issues for each queue
    private static Map<String, ErrorSpike> issueObjectMap = new HashMap<>();
    // Temporary counts for each queue
    private static Map<String, ErrorDetails> tempCounts = new HashMap<>();
    // this will delete issues as they are closed
    Set<String> objectsWithIssues = new HashSet<>();


    public static void countType1AuthError(String userId, String appName, String channelName, String connName, String CSPUserId) {
    	ErrorDetails currentDetails = tempCounts.getOrDefault(
    			"<QMGR - Auth>", 
    			new Auth1ErrorDetails(0, userId, appName,channelName,connName,CSPUserId));
    	currentDetails.incrementCount();
    	log.info("\"<QMGR - Auth>\" incremented to: " + currentDetails.getCount());
    	tempCounts.put("<QMGR - Auth>", currentDetails);
    	}
    
    // Count 2035 - 2 errors for the given queue and error code
    public static void countType2AuthError(String userId, String appName, String queueName) {
        ErrorDetails currentDetails = tempCounts.getOrDefault(queueName, new AuthErrorDetails(0, userId, appName));
        currentDetails.incrementCount();
        log.info(queueName + " incremented to: " + currentDetails.getCount());
        tempCounts.put(queueName, currentDetails);
    }
    
    public static void countUnknownObjectError(String appName, String connName, String channelName, String queueName) {
    	ErrorDetails currentDetails = tempCounts.getOrDefault(
    			"<QMGR - UnknownObject>", 
    			new UnknownObjectErrorDetails(0, appName, connName,channelName,queueName));
    	currentDetails.incrementCount();
    	log.info("\"<QMGR - UnknownObject>\" incremented to: " + currentDetails.getCount());
    	tempCounts.put("<QMGR - UnknownObject>", currentDetails);
    	}
    
    // TODO: potentially have a start time for the timestamp
    // Evaluate error rates and reset counts at a fixed rate
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateAndResetCounts() throws Exception {
    	// loading config information
    	
    	// load specific queue manger settings
    	QMConfig queueManagerConfig = 
    	configManager
    	.getConfig()
    	.getQms()
    	.getOrDefault(
    			qMgrName, 
    			configManager.getConfig().getQms().get("<DEFAULT>"));
    	
    	// get error threshold for queue manager
    	queueManagerThreshold = 
    	queueManagerConfig
    	.getQueueManager()
    	.getErrors()
    	.getMax();
    	
    	// get error threshold which applies to all queues
    	queueThreshold = 
    	queueManagerConfig
    	.getQueue()
    	.getErrors()
    	.getMax();
    	
    	
    	
    	System.out.println("queueManagerThreshold: " + queueManagerThreshold + " queueThreshold: " + queueThreshold);
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
            
            // TODO: for now using queue manager threshold for all - change this
            if (rate > queueManagerThreshold) {
            	// get or create issue for this queue or whole queue manager
            	ErrorSpike issue;
            	log.info("ErrorSpike issue for the queue manager, object: " + mqObject + ". Rate of error is: " + rate);
            	
            	// TODO: add achived info when initiating to technical details
            	if (mqObject == "<QMGR - Auth>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<QMGR>","<QMGR>"));
            		
            	} else if (mqObject == "<QMGR - UnknownObject>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2085s","<QMGR>","<QMGR>"));
            		
            	} else {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<QUEUE>",mqObject));
            	}

                // add new archieved info
                issue.addWindowData(errorInfo, rate);
                
                sender.sendIssue(issue);
                
                // we re-put into active issues
                issueObjectMap.put(mqObject, issue);
            }
            if (issueObjectMap.containsKey(mqObject)) {
                objectsWithIssues.add(mqObject);
            } 
        }
//        // sending to the accumulator
//        try {
//            issueAggregatorService.sendIssues("ErrorSpikeIssues", issueObjectMap);
//        } catch (Exception e) {
//            System.err.println("Failed to send issues to aggregator: " + e.getMessage());
//        }
        

        ConsoleLogger.printQueueCurrentIssues(issueObjectMap, "Error Rates");
        // Clear only queues without active issues
        tempCounts.keySet().removeAll(objectsWithIssues);

    }
}