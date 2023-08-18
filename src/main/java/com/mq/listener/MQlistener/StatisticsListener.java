package com.mq.listener.MQlistener;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.MQCFGR;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;

@Component
public class StatisticsListener {
    private static final Logger log = LoggerFactory.getLogger(StatisticsListener.class);
    
    
    // observedQueues stores queues from past messages, STATQ messages only include any one given queue if there's been some
    // operation on it (e.g. put, get,...). observedQueues allows us to interpolate 0's data where we don't recieve any data 
    // for any given queue
    
    // queueStatsMap records data from a single STATQ message (ie one period), it includes puts, gets,...
    
    // timeSeriesStats is a linked hashmap (allowing for chronology), where the key is the date-time of the retreval of 
    // a STATQ message and it contains a time-series of queueStatsMap's
    
    
    private static final Set<String> observedQueues = new HashSet<>();
    private static final Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();
    private static final Map<String, Map<String, Map<String, Integer>>> timeSeriesStats = new LinkedHashMap<>();
    
    @JmsListener(destination = "SYSTEM.ADMIN.STATISTICS.QUEUE")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
        	System.out.println("recieved stats message");
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                processStatQMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
    private void processStatQMessage(PCFMessage pcfMsg) {
        if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return;
        }
        try {
        	// get date/time upon recieving message
        	String currentDateTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);     
        	
        	Enumeration<?> parameters = pcfMsg.getParameters();
        	while (parameters.hasMoreElements()) {
	            PCFParameter parameter = (PCFParameter) parameters.nextElement();
	            
	            // for now assuming that MQCFGR is always a queue
	            // TODO: check that this is always a queue and in the same format
	            if (parameter instanceof MQCFGR) {
	            	// processing queue information
	                MQCFGR fgr = (MQCFGR) parameter;
	                Enumeration<?> nestedParameters = fgr.getParameters();
	                // processing the puts, gets,... for each queue into queueStatsMap
	                processQueue(nestedParameters);
	            }
    		}
        	// if observed queue not in the message, means there's been no operations in this period
        	// we're therefore making sure it's still in the queueStatsMap
            for (String queue : observedQueues) {
                if (!queueStatsMap.containsKey(queue)) {
                    Map<String, Integer> defaultStats = new HashMap<>();
                    defaultStats.put("PUTS", 0);
                    defaultStats.put("PUTS_FAILED", 0);
                    defaultStats.put("GETS", 0);
                    defaultStats.put("GETS_FAILED", 0);
                    queueStatsMap.put(queue, defaultStats);
                }
            }
            // now that all the data for this period is in queueStatsMap, we add it
            // to the timeSeriesStats, queueStatsMap is cleared each period because it's a static variable
            // and we're only interested in current data being in it
	        timeSeriesStats.put(currentDateTime, new HashMap<>(queueStatsMap));      
	        ensureMaxEntries();
	        printTimeSeriesStats();
            queueStatsMap.clear();
        } catch (RuntimeException e) {
            System.out.println("Error occurred while parsing PCFMessage: " + e.getMessage());
            e.printStackTrace();
            log.error("Error occurred while parsing PCFMessage: " + e.getMessage(), e);
        }
    }
    
    private static void printTimeSeriesStats() {
        timeSeriesStats.forEach((time, stats) -> {
            System.out.println("Time: " + time);
            stats.forEach((queueName, queueStats) -> {
                System.out.println("\tQueue Name: " + queueName);
                queueStats.forEach((key, value) -> {
                    System.out.println("\t\t" + key + ": " + value);
                });
            });
        });
    }
    
    /**
    When the size of timeSeriesStats gets to 30, this deletes the oldest entry, 
    to ensure not too much memory usage.
    */
    private static void ensureMaxEntries() {
        while (timeSeriesStats.size() > 30) {
        	// creating an iterator object it  which can 
            Iterator<String> i = timeSeriesStats.keySet().iterator();
            if (i.hasNext()) {
                i.next();
                i.remove();
            }
        }
    }
    
    private void processQueue(Enumeration<?> nestedParameters) {
    	// defining variables which we want to add to statsForQueue
        String QName = null;
        Integer quantPut = 0;
        Integer quantPutFails = 0;
        Integer quantGet = 0;
        Integer quantGetFails = 0;
        while (nestedParameters.hasMoreElements()) {
            PCFParameter nestedParameter = (PCFParameter) nestedParameters.nextElement();
            Object value = nestedParameter.getValue();
            switch (nestedParameter.getParameter()) {
                case MQConstants.MQCA_Q_NAME:
                    QName = nestedParameter.getStringValue();
                    break;
                // for now assuming puts and put1s are the same, etc.,
                case MQConstants.MQIAMO_PUTS:
                case MQConstants.MQIAMO_PUT1S:
                    if (value instanceof int[]) {
                        int firstElement = ((int[]) value)[0];
                        quantPut = quantPut + firstElement;
                    }
                    break;
                case MQConstants.MQIAMO_PUTS_FAILED:
                case MQConstants.MQIAMO_PUT1S_FAILED:
                	if (value instanceof Integer) {
                        Integer failedValue = (Integer) value;
                        quantPutFails = quantPutFails + failedValue;
                    }
                    break;
                case MQConstants.MQIAMO_GETS:
                    if (value instanceof int[]) {
                        int firstElement = ((int[]) value)[0];
                        quantGet = quantGet + firstElement;
                    }
                    break;
                case MQConstants.MQIAMO_GETS_FAILED:
                	if (value instanceof Integer) {
                        Integer failedValue = (Integer) value;
                        quantGetFails = quantGetFails + failedValue;
                    }
                    break;
        	}
    	}
        // adding the queue's stats to the queue stats map for this period - always a new map
        if(QName != null && !QName.contains("ADMIN") && !QName.contains("SYSTEM")) {
            observedQueues.add(QName);
            Map<String, Integer> statsForQueue = new HashMap<>();
            statsForQueue.put("PUTS", quantPut);
            statsForQueue.put("PUTS_FAILED", quantPutFails);
            statsForQueue.put("GETS", quantGet);
            statsForQueue.put("GETS_FAILED", quantGetFails);
            queueStatsMap.put(QName, statsForQueue);
        }
    }
    
}
