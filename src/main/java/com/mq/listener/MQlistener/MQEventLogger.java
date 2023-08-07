package com.mq.listener.MQlistener;



import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.json.JSONObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class MQEventLogger {

    private static final Logger logger = LoggerFactory.getLogger(MQEventLogger.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    public static void logEvent(PCFMessage pcfMessage) {
        StringBuilder sb = new StringBuilder();
        try {
	        sb.append("**** Message on Queue SYSTEM.ADMIN.QMGR.EVENT ****\n");
	        sb.append("Event Type                       : PCF Message\n");
	        sb.append("Reason                           : ").append(pcfMessage.getReason()).append(" (Unknown Alias Base Queue)\n");
	        // Assuming you can retrieve timestamp; otherwise, remove the following line.
//	        sb.append("Event created                    : ").append("TIMESTAMP_GOES_HERE").append("\n");
	        sb.append("  Queue Mgr Name                 : ").append(pcfMessage.getStringParameterValue(2015)).append("\n");
	        sb.append("  Queue Name                     : ").append(pcfMessage.getStringParameterValue(2016)).append("\n");
	        sb.append("  User Identifier                : ").append(pcfMessage.getStringParameterValue(3025)).append("\n");
	        sb.append("  Appl Type                      : ").append(pcfMessage.getIntParameterValue(1)).append(" (Unix)\n"); 
	        sb.append("  Appl Name                      : ").append(pcfMessage.getStringParameterValue(3024)).append("\n");
	        sb.append("  Open Options                   : ").append(pcfMessage.getIntParameterValue(1022)).append("\n");
        } catch (Exception e) {
        	logger.info("Error: {}", e);
        }
        
        


        logger.info(sb.toString());
    }
    
    public static void logEventJson(PCFMessage pcfMessage) {
        StringBuilder sb = new StringBuilder();
        JSONObject jsonOutput = new JSONObject();
        try {
	        sb.append("**** Message on Queue SYSTEM.ADMIN.QMGR.EVENT ****\n");
	        sb.append("Event Type                       : PCF Message\n");
	        sb.append("Reason                           : ").append(pcfMessage.getReason()).append(" (Unknown Alias Base Queue)\n");
	        // Assuming you can retrieve timestamp; otherwise, remove the following line.
//	        sb.append("Event created                    : ").append("TIMESTAMP_GOES_HERE").append("\n");
	        sb.append("  Queue Mgr Name                 : ").append(pcfMessage.getStringParameterValue(2015)).append("\n");
	        sb.append("  Queue Name                     : ").append(pcfMessage.getStringParameterValue(2016)).append("\n");
	        sb.append("  User Identifier                : ").append(pcfMessage.getStringParameterValue(3025)).append("\n");
	        sb.append("  Appl Type                      : ").append(pcfMessage.getIntParameterValue(1)).append(" (Unix)\n"); 
	        sb.append("  Appl Name                      : ").append(pcfMessage.getStringParameterValue(3024)).append("\n");
	        sb.append("  Open Options                   : ").append(pcfMessage.getIntParameterValue(1022)).append("\n");
        } catch (Exception e) {
        	logger.info("Error: {}", e);
        }
        
        JSONObject jsonOutput = new JSONObject();
        jsonOutput.put("JSON1", "Hello World!");
        String jsonString = jsonOutput.toString();
        logger.info(jsonString);
    }
    
//    public static void logEventAsJson(PCFMessage pcfMessage) {
//        try {
//            EventData eventData = new EventData();
//            eventData.setEventType("PCF Message");
//            eventData.setReason(pcfMessage.getReason() + " (Unknown Alias Base Queue)");
//            eventData.setQueueMgrName(pcfMessage.getStringParameterValue(2015));
//            eventData.setQueueName(pcfMessage.getStringParameterValue(2016));
//            eventData.setUserIdentifier(pcfMessage.getStringParameterValue(3025));
//            eventData.setApplType(pcfMessage.getIntParameterValue(1) + " (Unix)");
//            eventData.setApplName(pcfMessage.getStringParameterValue(3024));
//            eventData.setOpenOptions(pcfMessage.getIntParameterValue(1022));
//
//            String jsonOutput = objectMapper.writeValueAsString(eventData);
//            logger.info(jsonOutput);
//            objectMapper.writeValue(new File("d:/temp/output.json"), jsonOutput);
//        } catch (PCFException e) {
//            logger.error("Error converting PCF message to JSON", e);
//        } catch (JsonProcessingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//    }
//    
//    static class EventData {
//        private String eventType;
//        private String reason;
//        private String queueMgrName;
//        private String queueName;
//        private String userIdentifier;
//        private String applType;
//        private String applName;
//        private int openOptions;
//		public String getEventType() {
//			return eventType;
//		}
//		public void setEventType(String eventType) {
//			this.eventType = eventType;
//		}
//		public String getReason() {
//			return reason;
//		}
//		public void setReason(String reason) {
//			this.reason = reason;
//		}
//		public String getQueueMgrName() {
//			return queueMgrName;
//		}
//		public void setQueueMgrName(String queueMgrName) {
//			this.queueMgrName = queueMgrName;
//		}
//		public String getQueueName() {
//			return queueName;
//		}
//		public void setQueueName(String queueName) {
//			this.queueName = queueName;
//		}
//		public String getUserIdentifier() {
//			return userIdentifier;
//		}
//		public void setUserIdentifier(String userIdentifier) {
//			this.userIdentifier = userIdentifier;
//		}
//		public String getApplType() {
//			return applType;
//		}
//		public void setApplType(String applType) {
//			this.applType = applType;
//		}
//		public String getApplName() {
//			return applName;
//		}
//		public void setApplName(String applName) {
//			this.applName = applName;
//		}
//		public int getOpenOptions() {
//			return openOptions;
//		}
//		public void setOpenOptions(int openOptions) {
//			this.openOptions = openOptions;
//		}
//    }

//    public static void main(String[] args) {
//        // Sample usage; you'd typically use this method in response to receiving a PCF message.
//        PCFMessage sampleMessage = new PCFMessage(); // Create and set attributes as needed.
////        logEvent(sampleMessage);
//    }
}