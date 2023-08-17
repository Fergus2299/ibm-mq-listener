package com.mq.listener.MQlistener;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
    
    // storing a hashmap for each queue
    // outer map stores queue name
    // inner map has puts, gets,...
    private static final Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();
    private static final Map<String, Map<String, Map<String, Integer>>> timeSeriesStats = new LinkedHashMap<>();
    private static int periodCounter = 0;
    @JmsListener(destination = "SYSTEM.ADMIN.STATISTICS.QUEUE")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
        	System.out.println("recieved stats message");
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
//                System.out.println(pcfMsg);
                processStatQMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
                
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
    private static void processStatQMessage(PCFMessage pcfMsg) {
        if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return;
        }
        try {
        	// get date/time
        	String startDate = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_DATE).trim();
        	String startTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim();
        	String endDate = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_DATE).trim();
        	String endTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim();
        	
        	Enumeration<?> parameters = pcfMsg.getParameters();
        	while (parameters.hasMoreElements()) {
	            PCFParameter parameter = (PCFParameter) parameters.nextElement();
	            
	            // for now assuming that MQCFGR is always a queue
	            // TODO: check that this is always a queue and in the same format
	            if (parameter instanceof MQCFGR) {
	                MQCFGR fgr = (MQCFGR) parameter;
	                Enumeration<?> nestedParameters = fgr.getParameters();
	                // loop until there's no more elems in the Group 
	                
	                // for each queue we want the amount put and got 
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
//		                        System.out.println("Queue Name: " + QName);
		                        break;
		                    case MQConstants.MQIAMO_PUTS:
		                        if (value instanceof int[]) {
		                            int firstElement = ((int[]) value)[0];
		                            quantPut = quantPut + firstElement;
		                        }
		                        break;
		                    case MQConstants.MQIAMO_PUT1S:
		                        if (value instanceof int[]) {
		                            int firstElement = ((int[]) value)[0];
		                            quantPut = quantPut + firstElement;
		                        }
	                            break;
		                    case MQConstants.MQIAMO_PUTS_FAILED:
		                    	if (value instanceof Integer) {
		                            Integer failedValue = (Integer) value;
		                            quantPutFails = quantPutFails + failedValue;
		                        }
	                            break;
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
	                
	                // rudimentary removal of admin queues
	                if(QName != null && !QName.contains("ADMIN") && !QName.contains("SYSTEM")) {
	                    Map<String, Integer> statsForQueue = queueStatsMap.getOrDefault(QName, new HashMap<>());
	                    statsForQueue.put("PUTS", quantPut);
	                    statsForQueue.put("PUTS_FAILED", quantPutFails);
	                    statsForQueue.put("GETS", quantGet);
	                    statsForQueue.put("GETS_FAILED", quantGetFails);

	                    queueStatsMap.put(QName, statsForQueue);
	                	String timeKey = startDate + startTime + " - " + endDate + ":" + endTime;
		                timeSeriesStats.put(timeKey, new HashMap<>(queueStatsMap));      
		                ensureMaxEntries();
		                printTimeSeriesStats();
	                }

	            }
    		}
        } catch (RuntimeException e) {
            System.out.println("Error occurred while parsing PCFMessage: " + e.getMessage());
            e.printStackTrace();
            log.error("Error occurred while parsing PCFMessage: " + e.getMessage(), e);
        } catch (PCFException e) {
        	System.out.println("Error occurred while parsing PCFMessage: " + e);
        }
        periodCounter++;
        if (periodCounter >= 2) {
            generateCSVForAllQueues();
            periodCounter = 0;
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
    private static void ensureMaxEntries() {
        while (timeSeriesStats.size() > 30) {
            // Remove the oldest entry (which is the first one)
            Iterator<String> it = timeSeriesStats.keySet().iterator();
            if (it.hasNext()) {
                it.next();
                it.remove();
            }
        }
    }
    private static void generateCSVForAllQueues() {
    	System.out.println("generating report");
        timeSeriesStats.forEach((time, stats) -> {
            stats.forEach((queueName, queueStats) -> {
                String csvFileName = queueName + ".csv";
                try (FileWriter writer = new FileWriter(csvFileName, true)) {
                    writer.append(time);
                    for (String metric : Arrays.asList("PUTS", "PUTS_FAILED", "GETS", "GETS_FAILED")) {
                        writer.append(',');
                        writer.append(String.valueOf(queueStats.getOrDefault(metric, 0)));
                    }
                    writer.append('\n');
                } catch (IOException e) {
                    log.error("Error occurred while writing to CSV file: " + e.getMessage(), e);
                }
            });
        });
    }

  
}
