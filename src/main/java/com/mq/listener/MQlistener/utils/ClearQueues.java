package com.mq.listener.MQlistener.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;




/**
 * ClearQueues connects to the target MQ and clears all the relevant 
 * event queues which this application will listen to. 
 */

// TODO: credit the source of this class
@Component
@Order(2)
public class ClearQueues implements ApplicationRunner{

	
	// the lists being used
	// TODO: make user able to chose which issues are analysed
    private static final List<String> QUEUES = Arrays.asList(
            "SYSTEM.ADMIN.ACCOUNTING.QUEUE", 
            "SYSTEM.ADMIN.PERFM.EVENT", 
            "SYSTEM.ADMIN.QMGR.EVENT", 
            "SYSTEM.ADMIN.STATISTICS.QUEUE"
        );
	
	// getting from application properties
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;
	
    @Value("${ibm.mq.channel}")
    private String channel;

    @Value("${ibm.mq.connName}")
    private String connName;

    @Value("${ibm.mq.user}")
    private String user;

    @Value("${ibm.mq.password}")
    private String password;
    
    private String address;
    private Integer port;
    
    
    // extracting address and port from conn name
    public void extractConnName() {
    	Pattern pattern = Pattern.compile("^(.+)\\((\\d+)\\)$");
    	Matcher matcher = pattern.matcher(connName);
        if (matcher.find()) {
            address = matcher.group(1);
            port = Integer.parseInt(matcher.group(2));
            System.out.println("Address: " + address);
            System.out.println("Port: " + port);
        } else {
        	
        }
    }
    
   private Hashtable<String,Object> mqht;
   
   public ClearQueues()
   {
      mqht = new Hashtable<String,Object>();
   }
   
   private void init() throws IllegalArgumentException {
	   extractConnName();
	   
	   mqht.put(CMQC.CHANNEL_PROPERTY, channel);
	   mqht.put(CMQC.HOST_NAME_PROPERTY, address);
	   mqht.put(CMQC.PORT_PROPERTY, port);
	   mqht.put(CMQC.USER_ID_PROPERTY, user);
	   mqht.put(CMQC.PASSWORD_PROPERTY, password); 
   }
	   
   private Boolean doPCF(PCFMessageAgent agent, String queueName) {
	   PCFMessage   request   = null;
	   PCFMessage[] responses = null;
		   try {
	        // https://www.ibm.com/docs/en/ibm-mq/latest?topic=formats-clear-queue
	    	// sending the request
	        request = new PCFMessage(CMQCFC.MQCMD_CLEAR_Q);
	        request.addParameter(CMQC.MQCA_Q_NAME, queueName);
	        responses = agent.send(request);
	        System.out.println("responses.length="+responses.length);
	        
		        // checking response
		        for (int i = 0; i < responses.length; i++){
		           if ((responses[i]).getCompCode() == CMQC.MQCC_OK) {
		        	   System.out.println("Successfully cleared queue '"+queueName+"' of messages.");
		        	   return true;
		           }
		           else {
		        	   System.out.println("Error: Failed to clear queue '"+queueName+"' of messages.");
		           }
		        }
		   }
         catch (IOException e) {
            System.out.println("IOException:" +e.getLocalizedMessage());
         }
         catch (MQDataException e) {
            if ( (e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQCFC.MQRCCF_OBJECT_OPEN) ) {
               System.out.println("Error: Failed to clear queue '"+queueName+"' of messages. An application has the queue open.");
            }
            else {
               System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
            }
         }
		 return false;
     }
     
     @Override
     public void run(ApplicationArguments args) {
    	 Integer queuesCleared = 0;
         init();
         MQQueueManager qMgr = null;
         PCFMessageAgent agent = null;
         // connect to queue manger
         try {
             qMgr = new MQQueueManager(qMgrName, mqht);
             System.out.println("successfully connected to " + qMgrName);

             agent = new PCFMessageAgent(qMgr);
             System.out.println("successfully created agent");
             
             // as we clear queues we count them and ensure that all have been cleared
             for (String queue : QUEUES) {
                 if(doPCF(agent, queue)) {
                	 queuesCleared ++;
                 }
             }
         } catch (MQException e) {
             System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
	     } catch (MQDataException e) {
	    	  System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
         } finally {
	           try {
	              if (agent != null)
	              {
	                 agent.disconnect();
	                 System.out.println("disconnected from agent");
	              }
	           }
               catch (MQDataException e) {
                  System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
               }
               try {
                  if (qMgr != null)
                  {
                     qMgr.disconnect();
                     System.out.println("disconnected from "+ qMgrName);
                     // checking if all 4 queues cleared
                     System.out.println("successfully cleared " + queuesCleared + " queues");
                     if (queuesCleared >= 4) {
                    	 // TODO: send login success
                    	 return;
                     }
                  }
               }
               catch (MQException e) {
                  System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
               }
            }
         
      // TODO: send login failed
     }
}
