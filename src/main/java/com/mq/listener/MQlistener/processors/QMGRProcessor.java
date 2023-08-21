package com.mq.listener.MQlistener.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.issue_makers.AuthCounter;

public class QMGRProcessor {
    private static final Logger log = LoggerFactory.getLogger(QMGRProcessor.class);

    public static void processQMGRMessage(PCFMessage pcfMsg) {

    	try {
			MQCFH cfh = pcfMsg.getHeader();
			// reason code tells us what kind of QMGR event we're dealing with
	        int eventReason = cfh.getReason();
	        switch (eventReason) {
	        	case 2035:
	        		System.out.println("Received QMGR EVENT with a 2035 error!");
	        		// there are in total 7 types of 2035 error which are handled by this func
	        		process2035Message(pcfMsg);
	                break;
	            case 2085:
	                System.out.println("Received QMGR EVENT with a 2085 error!");
//	                System.out.println(pcfMsg);
	                break;
	        	default:
	            	System.out.println("Recieved QMGR EVENT which was not a 2035!");
	            	break;
	        }
    	} catch (Exception e) {
    		log.error("Error processing PCF message", e);
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
    			System.out.println("Recieved 2035 error of type 1!");
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
	            AuthCounter.countType1Error(userId, appName, channelName, connName, CSPUserId);
    			break;
    		// type 2: open not auth
    		case "MQRQ_OPEN_NOT_AUTHORIZED":
    			System.out.println("Recieved 2035 error of type 2!");
    			userId = pcfMsg.getStringParameterValue(MQConstants.MQCACF_USER_IDENTIFIER).trim();
	            appName = pcfMsg.getStringParameterValue(MQConstants.MQCACF_APPL_NAME).trim();
	            // not always returned
	            if (pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME) != null) {
	                QName = pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
	            } else {QName = "";}
	            
	            if (QName != "") {
	            	AuthCounter.countType2Error(userId, appName, QName);
	            } else {
	            	System.out.println("2035 type 2 had no queue associated with it!");
	            }
	            
    		    
    			break;
    			
    		// type 3: close not auth
    		case "MQRQ_CLOSE_NOT_AUTHORIZED":
    			System.out.println("Recieved 2035 error of type 3!");
    			break;
    		// type 4: command not auth
    		case "MQRQ_CMD_NOT_AUTHORIZED":
    			System.out.println("Recieved 2035 error of type 4!");
    			
    			break;
    		default:
    			System.out.println("Recieved 2035 error of unknown origin!");
            	break;
    			
    	}
    	
    	
    }


}