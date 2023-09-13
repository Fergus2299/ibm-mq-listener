package com.mq.listener.MQlistener.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

import com.ibm.mq.MQException;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.mq.listener.MQlistener.newConfig.Config;
import com.mq.listener.MQlistener.newConfig.ConfigManager;
// https://www.capitalware.com/rl_blog/?p=6603

// DISPLAY QSTATUS(SYSTEM.ADMIN.ACCOUNTING.QUEUE) TYPE(HANDLE) ALL ---- checks if app has queue open
@Component
@DependsOn("configManager")
public class StartupManager implements ApplicationRunner {
    private static final List<String> QUEUES = Arrays.asList(
            "SYSTEM.ADMIN.ACCOUNTING.QUEUE", 
            "SYSTEM.ADMIN.PERFM.EVENT", 
            "SYSTEM.ADMIN.QMGR.EVENT", 
            "SYSTEM.ADMIN.STATISTICS.QUEUE"
        );
    
    private Hashtable<String,Object> mqht = new Hashtable<String,Object>();
    
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
    
    @Autowired
    private ConfigManager configLoader;
    

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;
    
	@Autowired
	private SendLoginStatus sendLoginStatus;
	
    // var for checking queues were cleared successfully
    private Boolean clearQueueSuccess = false;
    private Boolean listenersStartSuccess = false;
//    the message which is sent to flask server
    private String returnMessage = "";
    
    @Override
    public void run(ApplicationArguments args) {
    	System.out.println("Start up");
        Config config = configLoader.getConfig();
        
        
        if (config != null) { // Or any other verification you want for the config
            clearAllEventQueues();
            startJmsListeners();
        } else {
            // TODO: Handle missing configuration
            System.out.println("Configuration is missing!");
        }
        sendFinalStatus();
    }
    
    private void sendFinalStatus() {
        if(clearQueueSuccess && listenersStartSuccess) {
            sendLoginStatus.sendStatus(true, "Login successful");
        } else {
            System.out.println(returnMessage);
            sendLoginStatus.sendStatus(false, returnMessage);
        }
    }
    
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
        	//TODO: handle failure
        	
        }
    }
    // once connected to a queue manager, this is run to clear a specific queue
    private Boolean clearQueue(PCFMessageAgent agent, String queueName) {
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

 		        	  returnMessage = "Error: Failed to clear queue '"+queueName+"' of messages.";
 		        	  System.out.println(returnMessage);
 		        	  
 		           }
 		        }
 		   }
          catch (IOException e) {
             returnMessage = "IOException:" +e.getLocalizedMessage();
             System.out.println(returnMessage);

          }
          catch (MQDataException e) {
        	   // TODO: check this: for now (development) many versions of this app can be running 
        	   // on different laptops. But this cannot happen in production because only one can take from queues.
             if ( (e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQCFC.MQRCCF_OBJECT_OPEN) ) {
            	 returnMessage = "Error: Failed to clear queue '"+queueName+"' of messages. An application has the queue open.";
                System.out.println(returnMessage);
                return true;
             }
             else {
            	 returnMessage = "CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]";
                System.out.println(returnMessage);
             }
          }
 		 return false;
      }
   
    // initiates the MQ connection
    private void initMQConnection() throws IllegalArgumentException {
 	   extractConnName();
 	   mqht.put(CMQC.CHANNEL_PROPERTY, channel);
 	   mqht.put(CMQC.HOST_NAME_PROPERTY, address);
 	   mqht.put(CMQC.PORT_PROPERTY, port);
 	   mqht.put(CMQC.USER_ID_PROPERTY, user);
 	   mqht.put(CMQC.PASSWORD_PROPERTY, password); 
// 	   mqht.put(CMQC.CONNECT_OPTIONS_PROPERTY, CMQC.MQCNO_RECONNECT_Q_MGR);
    }
    // handles clearing all relevant queues
    public void clearAllEventQueues() {
   	 Integer queuesCleared = 0;
        initMQConnection();
        MQQueueManager qMgr = null;
        PCFMessageAgent agent = null;
        // assigning a different thread to establish connection
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<MQQueueManager> future = executor.submit(() -> new MQQueueManager(qMgrName, mqht));
        // connect to queue manger
        try {
        	System.out.println("qm: " + qMgrName + "trying to connect with: " + mqht.toString());
            qMgr = future.get(5, TimeUnit.SECONDS);
        	qMgr = new MQQueueManager(qMgrName, mqht);
            System.out.println("successfully connected to " + qMgrName);
            agent = new PCFMessageAgent(qMgr);
            System.out.println("successfully created agent");
            // as we clear queues we count them and ensure that all have been cleared
            for (String queue : QUEUES) {
                if(!clearQueue(agent, queue)) {
                	// all queues must be cleared or the app will not function as intended
                	clearQueueSuccess = false;
               	 	return;
                }
            }
            

	     } catch (MQDataException e) {
	    	 System.out.println("error");
	    	 returnMessage = "CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]";
	    	  System.out.println(returnMessage);
        } catch (TimeoutException e) {
            System.out.println("Connection timed out!");
            returnMessage = "Connection to queue manager timed out.";
            future.cancel(true);
        } catch (Exception e) {
        	System.out.println("error");
        	returnMessage = "Connection to queue manager failed";
        	System.out.println(returnMessage);
        } finally {
            executor.shutdownNow();
        	System.out.println(clearQueueSuccess);
        	System.out.println(listenersStartSuccess);
        	}
        
        
        
        // now trying to disconnect
       try {
          if (agent != null) {
             agent.disconnect();
             System.out.println("disconnected from agent");
          }
       } catch (MQDataException e) {
    	  
    	  returnMessage = "CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]";
         System.out.println(returnMessage);
      }
      try {
         if (qMgr != null)
         {
            qMgr.disconnect();
            System.out.println("disconnected from "+ qMgrName);
            System.out.println("successfully cleared " + queuesCleared + " queues");
            clearQueueSuccess = true;
           	 return;
         }
      }
      catch (MQException e) {
    	 returnMessage = "CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]";
         System.out.println(returnMessage);
      }
    }
    
    private void startJmsListeners() {
    	// if clear queues wasn't successful then login info is wrong
    	// don't start listeners
    	if(!clearQueueSuccess) return;
        jmsListenerEndpointRegistry.start();
        
        // this section counts which of the 
        int count = 0;
        for (MessageListenerContainer container : jmsListenerEndpointRegistry.getListenerContainers()) {
            if (container instanceof AbstractMessageListenerContainer) {
                AbstractMessageListenerContainer abstractContainer = (AbstractMessageListenerContainer) container;
                if (abstractContainer.isRunning()) {
                    System.out.println("Listener for " + abstractContainer.getDestinationName() + " is running.");
                    count ++;
                }
            }
        }
        if (count >= 1) {
        	System.out.println("listenersStartSuccess");
        	listenersStartSuccess = true;
        } else {
        	returnMessage = "Listeners couldn't start";
        }
    	
    }
    
    
}