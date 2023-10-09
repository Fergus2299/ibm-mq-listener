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
import com.mq.listener.MQlistener.models.Errors.Auth1Error;
import com.mq.listener.MQlistener.models.Errors.AuthError;
import com.mq.listener.MQlistener.models.Errors.Error;
import com.mq.listener.MQlistener.models.Errors.UnknownObjectError;
import com.mq.listener.MQlistener.models.Issue.ErrorSpike;
import com.mq.listener.MQlistener.utils.ConsoleLogger;
import com.mq.listener.MQlistener.utils.IssueSender;

@Component
public class ErrorMetrics {
    private static final Logger log = LoggerFactory.getLogger(ErrorMetrics.class);
    
    private final ConfigManager configManager;
    int queueManagerThreshold;
    int queueThreshold;
    
    @Autowired
    public IssueSender sender;
    
    @Autowired
    public ErrorMetrics(ConfigManager configManager, IssueSender sender) {
        this.configManager = configManager;
        this.sender = sender;

    }
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;
	
	// minute time window
    private final long WINDOW_DURATION_MILLIS = 20 * 1000;
    private final long MILLIS_IN_MINUTE = 60 * 1000;
    
    // Active issues for each queue
    private Map<String, ErrorSpike> issueObjectMap = new HashMap<>();
    // Temporary counts for each queue
    private Map<String, Error> errorCount = new HashMap<>();


    
    public void countType1AuthError(String userId, String appName, String channelName, String connName, String CSPUserId) {
    	Error currentDetails = errorCount.getOrDefault(
    			"<QMGR - Auth>", 
    			new Auth1Error(0, userId, appName,channelName,connName,CSPUserId));
    	currentDetails.incrementCount();
    	log.info("\"<QMGR - Auth>\" incremented to: " + currentDetails.getCount());
    	errorCount.put("<QMGR - Auth>", currentDetails);
    	}
    
    // Count 2035 - 2 errors for the given queue and error code
    public void countType2AuthError(String userId, String appName, String queueName) {
        Error currentDetails = errorCount.getOrDefault(queueName, new AuthError(0, userId, appName));
        currentDetails.incrementCount();
        log.info(queueName + " incremented to: " + currentDetails.getCount());
        errorCount.put(queueName, currentDetails);
    }
    
    public void countUnknownObjectError(String appName, String connName, String channelName, String queueName) {
    	Error currentDetails = errorCount.getOrDefault(
    			"<QMGR - UnknownObject>", 
    			new UnknownObjectError(0, appName, connName,channelName,queueName));
    	currentDetails.incrementCount();
    	log.info("\"<QMGR - UnknownObject>\" incremented to: " + currentDetails.getCount());
    	errorCount.put("<QMGR - UnknownObject>", currentDetails);
    	}
    
    // Evaluate error rates and reset counts at a fixed rate
    @Scheduled(fixedRate = WINDOW_DURATION_MILLIS)
    public void evaluateMetrics() throws Exception {
    	// loading config information
    	
    	// load specific queue manger settings
    	QMConfig queueManagerConfig = 
    	configManager
    	.config
    	.getQms()
    	.getOrDefault(
    			qMgrName, 
    			configManager.config.getQms().get("<DEFAULT>"));
    	
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
    	
    	for (Map.Entry<String, Error> entry : errorCount.entrySet()) {       	
    	    String mqObject = entry.getKey();
    	    Error errorInfo = entry.getValue();

    	    // get the count
        	int count = errorInfo.getCount();
			ErrorSpike issue;
        	System.out.println("Count: " + count+ ", QMGR?: " + mqObject.contains("<QMGR>") +queueManagerThreshold + ", " + queueThreshold );
            if (mqObject.contains("QMGR") && count > queueManagerThreshold) {
            	// get or create issue for this queue or whole queue manager
            	
            	log.info("ErrorSpike issue for the queue manager, object: " + mqObject + ". Rate of errors per minute is: " + count);
            	if (mqObject == "<QMGR - Auth>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<QMGR>",qMgrName));
                    // add new archieved info
                    issue.addWindowData(errorInfo, count);
                    
                    sender.sendIssue(issue);
                    
                    // we re-put into active issues
                    issueObjectMap.put(mqObject, issue);
            		
            	} else if (mqObject == "<QMGR - UnknownObject>") {
            		issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2085s","<QMGR>",qMgrName));
                    // add new archieved info
                    issue.addWindowData(errorInfo, count);
                    
                    sender.sendIssue(issue);
                    
                    // we re-put into active issues
                    issueObjectMap.put(mqObject, issue);
            		
            	}
        } else if (!mqObject.contains("QMGR") && count > queueThreshold) {
        	issue = issueObjectMap.getOrDefault(mqObject, new ErrorSpike("Too_Many_2035s","<QUEUE>",mqObject));
            // add new archieved info
            issue.addWindowData(errorInfo, count);
            
            sender.sendIssue(issue);
            
            // we re-put into active issues
            issueObjectMap.put(mqObject, issue);
        }
        ConsoleLogger.printQueueCurrentIssues(issueObjectMap, "Error Metrics");
    	}
	}
	public IssueSender getSender() {
		return sender;
	}

	public void setSender(IssueSender sender) {
		this.sender = sender;
	}
    
}