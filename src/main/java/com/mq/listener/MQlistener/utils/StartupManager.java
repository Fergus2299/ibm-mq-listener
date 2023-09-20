package com.mq.listener.MQlistener.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import com.mq.listener.MQlistener.config.Config;
import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.config.QueueConfig;

// DISPLAY QSTATUS(SYSTEM.ADMIN.ACCOUNTING.QUEUE) TYPE(HANDLE) ALL ---- checks if app has queue open
@Component
@DependsOn("configManager")
public class StartupManager implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(StartupManager.class);

	@Autowired
	Utilities utilities;

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
    
    // only queues that can be loaded from config
    private static final Set<String> allowedQueues = new HashSet<>(Arrays.asList(
            "SYSTEM.ADMIN.ACCOUNTING.QUEUE",
            "SYSTEM.ADMIN.PERFM.EVENT",
            "SYSTEM.ADMIN.QMGR.EVENT",
            "SYSTEM.ADMIN.STATISTICS.QUEUE"
    ));
    
    @Autowired
    private ConfigManager configLoader;
    
    @Autowired
    private QueueConfig queueConfig;

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;
    
	@Autowired
	private SendLoginStatus sendLoginStatus;
	
    // var for checking queues were cleared successfully
	private Boolean loadConfig = false;
    private Boolean clearQueueSuccess = false;
    private Boolean listenersStartSuccess = false;
//    the message which is sent to flask server
    private String returnMessage = "";
    
    // extracting address and port from conn name
    public void extractConnName() throws Exception {
    	Pattern pattern = Pattern.compile("^(.+)\\((\\d+)\\)$");
    	Matcher matcher = pattern.matcher(connName);
        if (matcher.find()) {
            address = matcher.group(1);
            port = Integer.parseInt(matcher.group(2));
        } else {
        	throw new Exception("Incorrect IP Address or Application Config format.");
        }
    }
    
    @Override
    public void run(ApplicationArguments args) {
    	logger.info("StartupManager initialization started.");
    	
        Config config = configLoader.getConfig();
        logger.info("Queues to clear: " + queueConfig.toString());
        
        if (config != null) {
        	logger.info("Configuration loaded successfully.");
        	loadConfig = true;
        	try {
        		clearAllEventQueues();
        		startJmsListeners();
        	} catch (Exception e) {
        		// clearing event queues wasn't successful, we try and ge
        		logger.error("Exception occurred: {}", e.getMessage(), e); 
        		returnMessage = e.getMessage();
        	}
            
        } else {
        	// TODO: Handle missing configuration
            logger.error("Failed to load configuration.");
            returnMessage = "Configuration has failed to load.";
        }
        sendFinalStatus();
    }
    
    private void sendFinalStatus() {
        if(clearQueueSuccess && listenersStartSuccess && loadConfig) {
            sendLoginStatus.sendStatus(true, "Login successful");
            logger.info("Startup sequence completed successfully.");
        } else {
            System.out.println(returnMessage);
            sendLoginStatus.sendStatus(false, returnMessage);
            logger.warn("Startup sequence completed with errors: {}", returnMessage);
        }
    }
    

    // once connected to a queue manager, this is run to clear a specific queue
    private Boolean clearQueue(PCFMessageAgent agent, String queueName) throws Exception {
 	   PCFMessage   request   = null;
 	   PCFMessage[] responses = null;
 		   try {
 	        // https://www.ibm.com/docs/en/ibm-mq/latest?topic=formats-clear-queue
 	    	// sending the request
 	        request = new PCFMessage(CMQCFC.MQCMD_CLEAR_Q);
 	        request.addParameter(CMQC.MQCA_Q_NAME, queueName);
 	        responses = agent.send(request); 	        
 		        // checking response
 		        for (int i = 0; i < responses.length;) {
 		           if ((responses[i]).getCompCode() == CMQC.MQCC_OK) {
 		        	  logger.info("Successfully cleared queue '"+queueName+"' of messages.");
 		        	   return true;
 		           }
 		           else {
 		        	  throw new Exception("Error: Failed to clear queue '"+queueName+"' of messages.");
 		        	  
 		           }
 		        }
 		   }
          catch (IOException e) {
        	  throw new Exception("IOException:" + e.getLocalizedMessage());
          }
          catch (MQDataException e) {
        	   // TODO: check this: for now (development) many versions of this app can be running 
        	   // on different laptops. But this cannot happen in production because only one can take from queues.
             if ( (e.completionCode == CMQC.MQCC_FAILED) && (e.reasonCode == CMQCFC.MQRCCF_OBJECT_OPEN) ) {
            	 throw new Exception("Error: Failed to clear queue '"+queueName+"' of messages. An application has the queue open.");
             }
             else {
            	 throw new Exception("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
             }
          }
 		 return false;
      }
   
    // initiates the MQ connection
    private void initMQConnection() throws IllegalArgumentException, Exception {
 	   extractConnName();
 	   mqht.put(CMQC.CHANNEL_PROPERTY, channel);
 	   mqht.put(CMQC.HOST_NAME_PROPERTY, address);
 	   mqht.put(CMQC.PORT_PROPERTY, port);
 	   mqht.put(CMQC.USER_ID_PROPERTY, user);
 	   mqht.put(CMQC.PASSWORD_PROPERTY, password); 
    }
    // handles clearing all relevant queues
    public void clearAllEventQueues() throws Exception {
   	 Integer queuesCleared = 0;
        initMQConnection();
        MQQueueManager qMgr = null;
        PCFMessageAgent agent = null;
        // assigning a different thread to establish connection
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<MQQueueManager> future = executor.submit(() -> new MQQueueManager(qMgrName, mqht));
        // connect to queue manger
        try {
        	logger.info("Trying to connect to " + qMgrName + ", with: " + mqht.toString());
            qMgr = future.get(5, TimeUnit.SECONDS);
        	qMgr = new MQQueueManager(qMgrName, mqht);
        	logger.info("successfully connected to " + qMgrName);
            agent = new PCFMessageAgent(qMgr);
            logger.info("successfully created agent");
            // going through queues from config and clear them if they are in allowedQueues
            for (String queue : queueConfig.getQueues()) {
                if(!allowedQueues.contains(queue) || !clearQueue(agent, queue)) {
                	// all queues must be cleared or the app will not function as intended
                	clearQueueSuccess = false;
                	logger.error("Failed to clear: " + queue);
                	throw new Exception("Failed to clear: " + queue);
                }
            }
	    } catch (MQDataException e) {
	    	logger.error("Error connecting to queue manager.");
	    	 throw new Exception("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
        } catch (TimeoutException e) {
        	logger.error("Connection timed out!");
            future.cancel(true);
            throw new Exception("Connection to queue manager timed out.");
        } catch (Exception e) {
        	logger.error(e.getMessage());
        	throw new Exception(e.getMessage());
        } finally {
        	// deleting timed event
            executor.shutdownNow();
        }
        
        
       // now trying to disconnect
       try {
          if (agent != null) {
             agent.disconnect();
             logger.info("Successfully disconnected from agent");
          }
       } catch (MQDataException e) {
    	   
    	   throw new Exception("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
      }
      try {
         if (qMgr != null)
         {
            qMgr.disconnect();
            logger.info("disconnected from "+ qMgrName);
            clearQueueSuccess = true;
           	 return;
         }
      }
      catch (MQException e) {
    	  throw new Exception("CC=" +e.completionCode + " : RC=" + e.reasonCode + " [" + MQConstants.lookup(e.reasonCode, "MQRC_.*") + "]");
      }
    }
    
    private void startJmsListeners() throws Exception {
    	// if clear queues wasn't successful then login info is wrong
    	// don't start listeners
    	if(!clearQueueSuccess) return;
    	logger.info("Starting JMS listeners.");
        jmsListenerEndpointRegistry.start();
        List <String> configuredQueues = queueConfig.getQueues();
        // this section counts which of the 
        int count = 0;
        for (MessageListenerContainer container : jmsListenerEndpointRegistry.getListenerContainers()) {
            if (container instanceof AbstractMessageListenerContainer) {
                AbstractMessageListenerContainer abstractContainer = (AbstractMessageListenerContainer) container;
                
                String destinationName = abstractContainer.getDestinationName();
               
                // stop listener if in ignored queues
                if (!configuredQueues.contains(destinationName)) {
                    if (abstractContainer.isRunning()) {
                    	logger.info("Stopping listener for queue: {}", destinationName);
                        abstractContainer.stop();
                    }
                    continue;
                }
                
                if (abstractContainer.isRunning()) {
                	logger.info("Listener for queue {} is running.", destinationName);
                    count ++;
                }
            }
        }
        // check if all queues in queueConfig started
        if (count >= configuredQueues.size()) {
        	listenersStartSuccess = true;
        } else {
        	
        	throw new Exception("Listeners couldn't start, only " + count + " of " + configuredQueues.size() + " started.");
        }
    	
    }
    
    
}