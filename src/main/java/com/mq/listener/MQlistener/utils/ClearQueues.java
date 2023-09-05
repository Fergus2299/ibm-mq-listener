package com.mq.listener.MQlistener.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

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




// TODO: credit the source of this class
@Component
@Order(1)
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

   private Hashtable<String,Object> mqht;
   
   public ClearQueues()
   {
      mqht = new Hashtable<String,Object>();
   }
   
   private void init() throws IllegalArgumentException {
	   // temporarily using this
	   mqht.put(CMQC.CHANNEL_PROPERTY, channel);
	   mqht.put(CMQC.HOST_NAME_PROPERTY, "13.87.80.195");
	   mqht.put(CMQC.PORT_PROPERTY, 1414);
	   mqht.put(CMQC.USER_ID_PROPERTY, user);
	   mqht.put(CMQC.PASSWORD_PROPERTY, password); 
   }
	   
   private void doPCF(PCFMessageAgent agent, String queueName) {
         PCFMessage   request   = null;
         PCFMessage[] responses = null;
    
         try
         {
            // https://www.ibm.com/docs/en/ibm-mq/latest?topic=formats-clear-queue
        	// sending the request
            request = new PCFMessage(CMQCFC.MQCMD_CLEAR_Q);
            request.addParameter(CMQC.MQCA_Q_NAME, queueName);
            responses = agent.send(request);
            System.out.println("responses.length="+responses.length);
            
            // checking response
            for (int i = 0; i < responses.length; i++)
            {
               if ((responses[i]).getCompCode() == CMQC.MQCC_OK)
                  System.out.println("Successfully cleared queue '"+queueName+"' of messages.");
               else
                  System.out.println("Error: Failed to clear queue '"+queueName+"' of messages.");
            }
         }
         catch (IOException e)
         {
            System.out.println("IOException:" +e.getLocalizedMessage());
         }
         catch (MQDataException e)
         {
            if ( (e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQCFC.MQRCCF_OBJECT_OPEN) )
               System.out.println("Error: Failed to clear queue '"+queueName+"' of messages. An application has the queue open.");
            else
               System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
         }
         
     }
     
     @Override
     public void run(ApplicationArguments args) {
         init();
         MQQueueManager qMgr = null;
         PCFMessageAgent agent = null;
         // connect to queue manger
         try {
             qMgr = new MQQueueManager(qMgrName, mqht);
             System.out.println("successfully connected to " + qMgrName);

             agent = new PCFMessageAgent(qMgr);
             System.out.println("successfully created agent");

             // iterating through each queue we're looking at and clearing
             for (String queue : QUEUES) {
                 doPCF(agent, queue);
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
                  }
               }
               catch (MQException e) {
                  System.out.println("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
               }
            }
     }
}
