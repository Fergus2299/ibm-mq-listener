//package com.mq.listener.MQlistener.Listeners;
//
//import jakarta.jms.BytesMessage;
//import jakarta.jms.Message;
//import jakarta.jms.JMSException;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.json.JSONObject;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.jms.annotation.JmsListener;
//import org.springframework.stereotype.Component;
//
//import com.ibm.mq.headers.pcf.MQCFH;
//import com.ibm.mq.headers.pcf.PCFMessage;
//import com.mq.listener.MQlistener.models.QueueServiceHighIssue;
//import com.ibm.mq.MQMessage;
//import com.ibm.mq.constants.MQConstants;
//
//@Component
//public class PerformanceListener {
//	private static Map<String, QueueServiceHighIssue> issuesMap = new HashMap<>();
//    private static final Logger log = LoggerFactory.getLogger(AccountingListener.class);
//    @JmsListener(destination = "SYSTEM.ADMIN.PERFM.EVENT")
//    public void listen(Message receivedMessage) throws JMSException {
//        if (receivedMessage instanceof BytesMessage) {
//            try {
//                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
//                PCFMessage pcfMsg = new PCFMessage(mqMsg);
//                // send PCF message for processing
//                processPerformanceMessage(pcfMsg);
//            } catch (Exception e) {
//                MQListener.logProcessingError(e, "PCF");
//            }
//        } else {
//            log.warn("Received non-bytes message: {}", receivedMessage);
//        }
//    }
//    
//    private void processPerformanceMessage(PCFMessage pcfMsg) {
//		try {
//    		MQCFH cfh = pcfMsg.getHeader();
//    		int command = cfh.getCommand();
//            int reason = cfh.getReason();
//            
//            if (reason == 2226 && command == 45) {
//            	// MQRC_Q_SERVICE_INTERVAL_HIGH) and MQCMD_PERFM_EVENT
//            	// since these come rarely enough we can convert to json
////            	JSONObject msgJson = new JSONObject(PCFParser.toPcfMessageJson(pcfMsg));
//                String Q = pcfMsg.getStringParameterValue(MQConstants.MQCA_BASE_OBJECT_NAME).trim();
//                
//                // TODO: put the following in the issue description (for now)
//                Integer timeSinceReset = pcfMsg.getIntParameterValue(MQConstants.MQIA_TIME_SINCE_RESET);
//                Integer highQDepth = pcfMsg.getIntParameterValue(MQConstants.MQIA_HIGH_Q_DEPTH);
//                Integer enQCount = pcfMsg.getIntParameterValue(MQConstants.MQIA_MSG_ENQ_COUNT);
//                Integer DeQCount = pcfMsg.getIntParameterValue(MQConstants.MQIA_MSG_DEQ_COUNT);
//
//
//            	QueueServiceHighIssue issue = new QueueServiceHighIssue(Q);
//            	addIssue(Q, issue);
//            	
//            } else if (reason == 2227 && command == 45) {
//            	// MQRC_Q_SERVICE_INTERVAL_OK) and MQCMD_PERFM_EVENT
//            } else {
//            	System.out.println("Recieved Non-Queue service interval message from SYSTEM.ADMIN.PERFM.EVENT");
//            }
//
//    	} catch (Exception e) {
//    		log.error("Error processing PCF message", e);
//    	}
//    }
//    
//    private static void addIssue(String Q, QueueServiceHighIssue issue) {
//        issuesMap.putIfAbsent(Q, issue);
//    }
//}