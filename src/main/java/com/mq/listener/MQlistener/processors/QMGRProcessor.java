package com.mq.listener.MQlistener.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.metrics.ErrorMetrics;
import com.mq.listener.MQlistener.parsers.PCFParser;

public class QMGRProcessor {
    private static final Logger log = LoggerFactory.getLogger(QMGRProcessor.class);

    public static void processQMGRMessage(PCFMessage pcfMsg) {

    	try {
    		// TODO: test all cases where you recieve a message which is not a handled case
			MQCFH cfh = pcfMsg.getHeader();
			// reason code tells us what kind of QMGR event we're dealing with
	        int eventReason = cfh.getReason();
	        switch (eventReason) {
	        	case 2035:
//	        		log.info("Received QMGR EVENT with a 2035 error!");
	        		// there are in total 7 types of 2035 error which are handled by this func
	        		process2035Message(pcfMsg);
	                break;
	            case 2085:
//	            	log.info("Received QMGR EVENT with a 2035 error!");
//	                System.out.println("Received QMGR EVENT with a 2085 error!");
	                process2085Message(pcfMsg);
	                break;
	        	default:
	            	System.out.println("Recieved QMGR EVENT which was not a 2035!");
	            	PCFParser.parsePCFMessage(pcfMsg);
	            	break;
	        }
    	} catch (Exception e) {
    		log.error("Error processing QMGR PCF message", e);
    		PCFParser.parsePCFMessage(pcfMsg);
    	}
    }

    private static void process2035Message(PCFMessage pcfMsg) throws PCFException {
    	// might have a queue associated with it or a channel
    	
    	// all 2035's will have a reason qualifier
		// clarify what kind of 2035 error. e.g., RQ = 1 means MQRQ_CONN_NOT_AUTHORIZED
		// or RQ = 2 means MQRQ_OPEN_NOT_AUTHORIZED
    	String RQ = MQConstants.lookup(pcfMsg.getIntParameterValue(MQConstants.MQIACF_REASON_QUALIFIER), "MQRQ_.*");
    	// for now we're handling type 1 to 4 - mainly point to point messaging
    	
    	// TODO: extend issue making to pub-sub
    	String appName;
    	String userId;
    	String QName;
    	String channelName;
    	String connName;
    	String CSPUserId;
    	
    	switch (RQ) {
    		// type 1: connection not auth
    		case "MQRQ_CONN_NOT_AUTHORIZED":
    		case "MQRQ_SYS_CONN_NOT_AUTHORIZED":
    		case "MQRQ_CSP_NOT_AUTHORIZED":
    			log.info("Recieved 2035 error of type 1!");
    			userId = pcfMsg.getStringParameterValue(MQConstants.MQCACF_USER_IDENTIFIER).trim();
	            appName = pcfMsg.getStringParameterValue(MQConstants.MQCACF_APPL_NAME).trim();
	            if (pcfMsg.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME) != null) {
	                channelName = pcfMsg.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME).trim();
	            } else {channelName = "";}
	            if (pcfMsg.getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME) != null) {
	                connName = pcfMsg.getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME).trim();
	            } else {connName = "";}
	            if (pcfMsg.getStringParameterValue(MQConstants.MQCACF_CSP_USER_IDENTIFIER) != null) {
	            	CSPUserId = pcfMsg.getStringParameterValue(MQConstants.MQCACF_CSP_USER_IDENTIFIER).trim();
	            } else {CSPUserId = "";}
	            ErrorMetrics.countType1AuthError(userId, appName, channelName, connName, CSPUserId);
    			break;
    		// type 2: open not auth
    		case "MQRQ_OPEN_NOT_AUTHORIZED":
    			log.info("Recieved 2035 error of type 2!");
    			userId = pcfMsg.getStringParameterValue(MQConstants.MQCACF_USER_IDENTIFIER).trim();
	            appName = pcfMsg.getStringParameterValue(MQConstants.MQCACF_APPL_NAME).trim();
	            // not always returned
	            if (pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME) != null) {
	                QName = pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
	            } else {QName = "";}
	            
	            if (QName != "") {
	            	ErrorMetrics.countType2AuthError(userId, appName, QName);
	            } else {
	            	System.out.println("2035 type 2 had no queue associated with it!");
	            }
	            
    		    
    			break;
    			
    		// type 3: close not auth
    		case "MQRQ_CLOSE_NOT_AUTHORIZED":
    			log.info("Recieved 2035 error of type 3!");
    			break;
    		// type 4: command not auth
    		case "MQRQ_CMD_NOT_AUTHORIZED":
    			log.info("Recieved 2035 error of type 4!");
    			break;
    		default:
    			log.error("Recieved 2035 error of unknown origin!");
            	break;
    	}
    }
    private static void process2085Message(PCFMessage pcfMsg) throws PCFException {
    	
    	// The 2085 means some MQOPEN or PUT1 command was unsuccessful due to trying to 
    	// access an object which doesn't exist. more info: https://www.ibm.com/docs/en/ibm-mq/9.3?topic=descriptions-unknown-object-name
    	String appName;
    	String connName;
    	String channelName;
    	String QName;
    	
        appName = pcfMsg.getStringParameterValue(MQConstants.MQCACF_APPL_NAME).trim();
        if (pcfMsg.getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME) != null) {
            connName = pcfMsg.getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME).trim();
        } else {connName = "";}
        if (pcfMsg.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME) != null) {
            channelName = pcfMsg.getStringParameterValue(MQConstants.MQCACH_CHANNEL_NAME).trim();
        } else {channelName = "";}
        if (pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME) != null) {
            QName = pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
        } else {QName = "";}
        
        // there are cases where a user might try and put to a topic (not a queue)
        // for now our app is ignoring this case. - This could be a future development
        if (QName != "") {
        	ErrorMetrics.countUnknownObjectError(appName, connName, channelName, QName);
        } else {
        	log.error("2035 type 2 had no queue associated with it. Could not process PCF!");
        	PCFParser.parsePCFMessage(pcfMsg);
        }
    }
}
