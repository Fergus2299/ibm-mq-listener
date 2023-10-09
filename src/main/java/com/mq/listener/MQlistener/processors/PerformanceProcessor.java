package com.mq.listener.MQlistener.processors;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.models.Issue.QueueServiceHighIssue;
import com.mq.listener.MQlistener.parsers.PCFParser;
import com.mq.listener.MQlistener.utils.IssueSender;

@Component
public class PerformanceProcessor {
    private static final Logger log = LoggerFactory.getLogger(PerformanceProcessor.class);
    @Autowired
    private IssueSender sender;
    
    // stores queue service high event issue, if we get the service ok event then
    // it updates the value in the map. If another service high event. Then the old issue is replaced
	private static Map<String, QueueServiceHighIssue> issueObjectMap = new HashMap<>();
    public void processPerformanceMessage(PCFMessage pcfMsg) {
		try {
			// check reason code
            String reasonCodeString = PCFParser.extractReasonCode(pcfMsg);
            switch (reasonCodeString) {
	    		case "MQRC_Q_SERVICE_INTERVAL_HIGH":
			    	// MQRC_Q_SERVICE_INTERVAL_HIGH) and MQCMD_PERFM_EVENT
			    	// https://www.ibm.com/docs/en/ibm-mq/9.3?topic=descriptions-queue-service-interval-high
	    			log.info("Recieved MQRC_Q_SERVICE_INTERVAL_HIGH event.");
			        String Q = pcfMsg.getStringParameterValue(MQConstants.MQCA_BASE_OBJECT_NAME).trim();
			        Integer timeSinceReset = pcfMsg.getIntParameterValue(MQConstants.MQIA_TIME_SINCE_RESET);
			        Integer highQDepth = pcfMsg.getIntParameterValue(MQConstants.MQIA_HIGH_Q_DEPTH);
			        Integer enQCount = pcfMsg.getIntParameterValue(MQConstants.MQIA_MSG_ENQ_COUNT);
			        Integer deQCount = pcfMsg.getIntParameterValue(MQConstants.MQIA_MSG_DEQ_COUNT);
			        
			        // create the issue
			        log.info("Creating QueueServiceHighIssue for: " + Q);
			    	QueueServiceHighIssue issue = new QueueServiceHighIssue(Q, timeSinceReset, highQDepth, enQCount, deQCount);
			    	// either replace an old one or put in this new one
			    	issueObjectMap.put(Q, issue);
			        // sending to frontend
			    	sender.sendIssue(issue);
	                break;
	                
	    		case "MQRC_Q_SERVICE_INTERVAL_OK":
	            	// MQRC_Q_SERVICE_INTERVAL_OK) and MQCMD_PERFM_EVENT
	    			log.info("Recieved MQRC_Q_SERVICE_INTERVAL_OK event.");
	    			String QOk = pcfMsg.getStringParameterValue(MQConstants.MQCA_BASE_OBJECT_NAME).trim();
	    			// if the queue is now ok, we send the new info to the frontend
	                QueueServiceHighIssue okIssue = issueObjectMap.get(QOk);
	                // we update the issue for that queue
	                if (okIssue != null) {
	                	log.info("Editing QueueServiceHighIssue (now OK) for: " + QOk);
	                    okIssue.okEventReceived();
	                    sender.sendIssue(okIssue);
	                }
	    			break;
	            default:
	            	log.info("Recieved Non-Queue service interval message from SYSTEM.ADMIN.PERFM.EVENT");
	            	PCFParser.parsePCFMessage(pcfMsg);
	            	break;
            }
    	} catch (Exception e) {
    		log.error("Error processing performance PCF message", e);
    		PCFParser.parsePCFMessage(pcfMsg);
    	}
    }
    

}
