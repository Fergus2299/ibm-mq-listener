package com.mq.listener.MQlistener.processors;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.models.Issue.QueueServiceHighIssue;
import com.mq.listener.MQlistener.parsers.PCFParser;
import com.mq.listener.MQlistener.utils.IssueAggregatorService;

public class PerformanceProcessor {
    private static final Logger log = LoggerFactory.getLogger(PerformanceProcessor.class);
    
    @Autowired
    private static IssueAggregatorService aggregatorService;
    
	private static Map<String, QueueServiceHighIssue> issueObjectMap = new HashMap<>();
    public static void processPerformanceMessage(PCFMessage pcfMsg) {
		try {
    		MQCFH cfh = pcfMsg.getHeader();
    		int command = cfh.getCommand();
            int reason = cfh.getReason();
            
            if (reason == 2226 && command == 45) {
            	// MQRC_Q_SERVICE_INTERVAL_HIGH) and MQCMD_PERFM_EVENT
            	// https://www.ibm.com/docs/en/ibm-mq/9.3?topic=descriptions-queue-service-interval-high
            	// these vals are always in this event message
                String Q = pcfMsg.getStringParameterValue(MQConstants.MQCA_BASE_OBJECT_NAME).trim();
                // Time, in seconds, since the statistics were last reset. For a service interval high event, this value is greater than the service interval.
                Integer timeSinceReset = pcfMsg.getIntParameterValue(MQConstants.MQIA_TIME_SINCE_RESET);
                // Maximum number of messages on the queue since the queue statistics were last reset.
                Integer highQDepth = pcfMsg.getIntParameterValue(MQConstants.MQIA_HIGH_Q_DEPTH);
                // Number of messages enqueued. This is the number of messages put on the queue since the queue statistics were last reset.
                Integer enQCount = pcfMsg.getIntParameterValue(MQConstants.MQIA_MSG_ENQ_COUNT);
                // Number of messages removed from the queue since the queue statistics were last reset.
                Integer deQCount = pcfMsg.getIntParameterValue(MQConstants.MQIA_MSG_DEQ_COUNT);

            	QueueServiceHighIssue issue = new QueueServiceHighIssue(Q, timeSinceReset, highQDepth, enQCount, deQCount);
            	issueObjectMap.putIfAbsent(Q, issue);
                // sending to the accumulator
                try {
                	IssueAggregatorService.sendIssues("PerformanceIssues", issueObjectMap);
                } catch (Exception e) {
                    System.err.println("Failed to send issues to aggregator: " + e.getMessage());
                }
            	
            	
            } else if (reason == 2227 && command == 45) {
            	// MQRC_Q_SERVICE_INTERVAL_OK) and MQCMD_PERFM_EVENT
            } else {
            	System.out.println("Recieved Non-Queue service interval message from SYSTEM.ADMIN.PERFM.EVENT");
            }

    	} catch (Exception e) {
    		log.error("Error processing performance PCF message", e);
    		PCFParser.parsePCFMessage(pcfMsg);
    	}
    }
    

}
