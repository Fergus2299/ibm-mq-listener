package com.mq.listener.MQlistener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;

//TODO: We only want to detect errors while the tool is turned on, it might be advisable to
// delete everything off the event queues when the app starts. - Might be worth advising IBM MQ
// to include date-time for events because of this because then we could have used this information.
@Component
public class MQListener {
    private static final Logger log = LoggerFactory.getLogger(MQListener.class);

    @JmsListener(destination = "SYSTEM.ADMIN.QMGR.EVENT")
    public void QMGRListener(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
        	
        	// getting the BytesMesage
            BytesMessage bytesMessage = (BytesMessage) receivedMessage;
            byte[] bytesreceived = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytesreceived);

            // convert to MQMessage then to PCFMessage
            MQMessage mqMsg = new MQMessage();
            try {
                mqMsg.write(bytesreceived);
                mqMsg.encoding = receivedMessage.getIntProperty("JMS_IBM_Encoding");
                mqMsg.format = receivedMessage.getStringProperty("JMS_IBM_Format");
                mqMsg.seek(0);

                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                // method for logging output
//                PCFParser.parsePCFMessage(pcfMsg);
                
                // getting the reason code
                MQCFH cfh = pcfMsg.getHeader();
                // we always need the reason because it's necessary fo r diagnosing the problem
    	        int eventReason = cfh.getReason();
    	        // we always need QM because the user might switch QM and then the log files are for a
    	        // different QM.
//                String eventQueueManagerName = pcfMsg.getStringParameterValue(MQConstants.MQCA_QM_NAME).trim();
                if (eventReason == 2035) {
        	        // getting the queue
                    String eventQueueName = pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
                    // we add it to the count of 2035 errors, and check it hasn't gone above 
                    ErrorCounter.countError(eventQueueName, eventReason);
                } else {
                	System.out.println("Recieved QMGR EVENT which was not a 2035!");
                }
			    
                // getting the event info into a json and creating file
//                String eventHeaderJson = PCFParser.toPcfMessageJson(pcfMsg);
//                try (FileWriter file = new FileWriter("eventHeader.json")) {
//                    file.write(eventHeaderJson);
//                    System.out.println("Successfully written JSON to " + "eventHeader.json");
//                } catch (IOException e) {
//                    System.out.println("An error occurred while writing to the file: " + e.getMessage());
//                }
                
                
                
            } catch (Exception e) {
                log.error("Error processing PCF message", e);
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
    /**
     * Accounting Listener Method
     *
     * This listener is responsible for monitoring the IBM MQ accounting data, gathered from the SYSTEM.ADMIN.ACCOUNTING.QUEUE.
     *
     * The accounting feature in IBM MQ needs to be enabled using the following MQSC commands:
     * 
     * 1. ALTER QMGR ACCTMQI(ON): Enables MQI accounting, which collects data about MQI calls made by applications.
     * 2. ALTER QMGR ACCTINT(interval): Sets the interval for gathering MQI accounting data, where 'interval' is in seconds.
     * 3. ALTER QMGR STATQ(ON): Optional, turns on queue statistics, which provide details about queue usage.
     *
     * Example of enabling MQI accounting with a 30-minute interval:
     *  - ALTER QMGR ACCTMQI(ON)
     *  - ALTER QMGR ACCTINT(1800)
     *
     * This listener method processes the accounting messages, extracting relevant information, and can be customized
     * to perform actions such as logging, analyzing, or forwarding the accounting data.
     *
     * Note: Enabling accounting may have an impact on system performance, and it can generate a significant
     * amount of data depending on application activity. Make sure to manage and monitor the data effectively.
     *
     * @throws JMSException if an error occurs while processing the message
     */
    
    
    @JmsListener(destination = "SYSTEM.ADMIN.ACCOUNTING.QUEUE")
    public void AccountingListener(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
        	
            // Getting the BytesMessage
            BytesMessage bytesMessage = (BytesMessage) receivedMessage;
            byte[] bytesReceived = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytesReceived);

            // Convert to MQMessage 
            MQMessage mqMsg = new MQMessage();
            try {
                mqMsg.write(bytesReceived);
                mqMsg.encoding = receivedMessage.getIntProperty("JMS_IBM_Encoding");
                mqMsg.format = receivedMessage.getStringProperty("JMS_IBM_Format");
                mqMsg.seek(0);
                
                // try to convert to PCF message
                PCFMessage pcfMsg = new PCFMessage(mqMsg);

                // TODO: Further process the MQ Accounting message as per your requirements
                log.info("Received accounting message: {}", pcfMsg);

                // Optionally, convert and save the accounting data to another format, e.g. JSON
                // This requires you to build a function to transform MQMessage to JSON.
                // String accountingDataJson = MQMessageToJson(mqMsg);
                // SaveJsonToFile(accountingDataJson, "accountingData.json");

            } catch (Exception e) {
                log.error("Error processing MQ Accounting message", e);
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
    
    
}