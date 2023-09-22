package com.mq.listener.MQlistener.metrics;

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

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.config.Config.QMConfig;
import com.mq.listener.MQlistener.models.Errors.Auth1ErrorDetails;
import com.mq.listener.MQlistener.models.Errors.AuthErrorDetails;
import com.mq.listener.MQlistener.models.Errors.ErrorDetails;
import com.mq.listener.MQlistener.models.Errors.UnknownObjectErrorDetails;
import com.mq.listener.MQlistener.models.Issue.ErrorSpike;
import com.mq.listener.MQlistener.utils.IssueSender;

@Component
public class ErrorMetrics {
    private static final Logger log = LoggerFactory.getLogger(ErrorMetrics.class);
    

    private final ConfigManager configManager;
    int queueManagerThreshold;
    int queueThreshold;
    
    @Autowired
    private IssueSender sender;
    
    @Autowired
    public ErrorMetrics(ConfigManager configManager) {
        this.configManager = configManager;

    }
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;
	
	// minute time window
    private static final long WINDOW_DURATION_MILLIS = 60 * 1000;
    private static final long MILLIS_IN_MINUTE = 60 * 1000;
    
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
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<QMGR>",qMgrName));
            		
            	} else if (mqObject == "<QMGR - UnknownObject>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2085s","<QMGR>",qMgrName));
            		
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
        // Clear only queues without active issues
        tempCounts.keySet().removeAll(objectsWithIssues);

    }
}