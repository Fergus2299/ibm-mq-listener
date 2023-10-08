package com.mq.listener.MQlistener.processors;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.MQCFGR;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.mq.listener.MQlistener.metrics.StatisticsMetrics;
import com.mq.listener.MQlistener.parsers.PCFParser;

@Service
public class StatisticsProcessor {
    private static final Logger log = LoggerFactory.getLogger(StatisticsMetrics.class);
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH.mm.ss");
	
	@Autowired
	StatisticsMetrics statisticsMetrics;
	
//    private static final Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();

    private static final Set<String> observedQueues = new HashSet<>();

    public void processStatisticsMessage(PCFMessage pcfMsg) throws Exception {
    	if (pcfMsg == null) {
    		
            log.error("Provided PCFMessage is null.");
            return;
        }


		String commandString = PCFParser.extractCommandString(pcfMsg);
//		System.out.println("Recieved Stats message, command: " + commandString);
		
		switch (commandString) {
			case "MQCMD_STATISTICS_Q":
				System.out.println("MQCMD_STATISTICS_Q");
				processStatQMessage(pcfMsg);
				break;
			case "MQCMD_STATISTICS_MQI":
				System.out.println("MQCMD_STATISTICS_MQI");
//				PCFParser.parsePCFMessage(pcfMsg);
				processStatMQIMessage(pcfMsg);
				
				break;
			default:
				System.out.println("STATS MESSAGE OF UNKNOWN TYPE");
				break;
		}
    }
    
    
    public void processStatMQIMessage(PCFMessage pcfMsg) throws Exception {
    	
    	
    	// MQIAMO_OPENS is https://www.ibm.com/docs/en/ibm-mq/9.3?topic=reference-notes#q037510___q037510_1
        String startTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim();
        String endTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim();
        int conns = 0, 
    		connsFailed = 0, 
    		opens = 0, 
    		opensFailed = 0, 
    		puts = 0, 
			putsFailed = 0, 
			put1s = 0, 
			put1sFailed = 0, 
			gets = 0, 
			getsFailed = 0;

     // For MQIAMO_CONNS
        try {
            conns = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_CONNS);
        } catch (Exception e) {
            conns = 0;
            log.warn("Failure getting MQIAMO_CONNS data.");
        }

     // For MQIAMO_CONNS_FAILED
        try {
            connsFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_CONNS_FAILED);
        } catch (Exception e) {
            connsFailed = 0;
            log.warn("Failure getting MQIAMO_CONNS_FAILED data.");
        }


        // For MQIAMO_OPENS
        try {
            int[] opensArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_OPENS);
            if (opensArray != null) {
                opens = Arrays.stream(opensArray).sum();
            } else {
                opens = 0;
            }
        } catch (Exception e) { 
            opens = 0;
            log.warn("Failure getting MQIAMO_OPENS data.");
        }

        // For MQIAMO_OPENS_FAILED
        try {
            int[] opensFailedArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_OPENS_FAILED);
            if (opensFailedArray != null) {
                opensFailed = Arrays.stream(opensFailedArray).sum();
            } else {
                opensFailed = 0;
            }
        } catch (Exception e) {
            opensFailed = 0;
            log.warn("Failure getting MQIAMO_OPENS_FAILED data.");
        }

     // For MQIAMO_PUTS
        try {
            int[] putsArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUTS);
            if (putsArray != null) {
                puts = Arrays.stream(putsArray).sum();
            } else {
                puts = 0;
            }
        } catch (Exception e) {
            puts = 0;
            log.warn("Failure getting MQIAMO_PUTS data.");
        }

     // For MQIAMO_PUTS_FAILED
        try {
            putsFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUTS_FAILED);
        } catch (Exception e) {
            putsFailed = 0;
            log.warn("Failure getting MQIAMO_PUTS_FAILED data.");
        }

        // For MQIAMO_PUT1S
        try {
            int[] put1sArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUT1S);
            if (put1sArray != null) {
                put1s = Arrays.stream(put1sArray).sum();
            } else {
                put1s = 0;
            }
        } catch (Exception e) {
            put1s = 0;
            log.warn("Failure getting MQIAMO_PUT1S data.");
        }



        // For MQIAMO_GETS
        try {
            int[] getsArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_GETS);
            if (getsArray != null) {
                gets = Arrays.stream(getsArray).sum();
            } else {
                gets = 0;
            }
        } catch (Exception e) {
            gets = 0;
            log.warn("Failure getting MQIAMO_GETS data.");
        }
        
        // For MQIAMO_PUT1S_FAILED
        try {
            put1sFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUT1S_FAILED);
        } catch (Exception e) {
            put1sFailed = 0;
            log.warn("Failure getting MQIAMO_PUT1S_FAILED data.");
        }

     // For MQIAMO_GETS_FAILED
        try {
            getsFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_GETS_FAILED);
        } catch (Exception e) {
            getsFailed = 0;
            log.warn("Failure getting MQIAMO_GETS_FAILED data.");
        }
        
        // puts and put1s are very similar
        puts = puts + put1s;
        putsFailed = putsFailed + put1sFailed;
        
        // TODO: put1s might affect opens and closes - might need to add to them too, research
        Map<String, Integer> statsForQM = new HashMap<>();
        statsForQM.put("CONNS", conns);
        statsForQM.put("CONNS_FAILED", connsFailed);
        statsForQM.put("OPENS", opens);
        statsForQM.put("OPENS_FAILED", opensFailed);
        statsForQM.put("PUTS", puts);
        statsForQM.put("PUTS_FAILED", putsFailed);
        statsForQM.put("GETS", gets);
        statsForQM.put("GETS_FAILED", getsFailed);

        LocalTime startTimeFormatted = LocalTime.parse(startTime, formatter);
        LocalTime endTimeFormatted = LocalTime.parse(endTime, formatter);
        statisticsMetrics.addQMTimeSeriesStats(startTimeFormatted, endTimeFormatted, statsForQM);
    }

    
	public void processStatQMessage(PCFMessage pcfMsg) throws Exception {
		// this is the type fthat we're dealing with: https://www.ibm.com/docs/en/ibm-mq/9.3?topic=reference-queue-statistics-message-data
		Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();
	    try {
	    	Enumeration<?> parameters = pcfMsg.getParameters();
	    	while (parameters.hasMoreElements()) {
	            PCFParameter parameter = (PCFParameter) parameters.nextElement();
	            
	            // In this instance MQCFGR parameter will always be a queue
	            if (parameter instanceof MQCFGR) {
	            	// processing queue information
	                MQCFGR fgr = (MQCFGR) parameter;
	                Enumeration<?> nestedParameters = fgr.getParameters();
	                
	                // processing the puts, gets,... for each queue into queueStatsMap
	                Map<String, Map<String, Integer>> individualQueueStatsMap = processQueue(nestedParameters);
	                // if the individualQueueStatsMap is empty the it was from a queue which contains data we're not interested in 
	                if (!individualQueueStatsMap.isEmpty()) {
	                	queueStatsMap.putAll(individualQueueStatsMap);
	                }
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
	        
	        
	        // get the time interval of the message
	        String startTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim();
	        LocalTime startTimeFormatted = LocalTime.parse(startTime, formatter);
	        String endTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim();
	        LocalTime endTimeFormatted = LocalTime.parse(endTime, formatter);
	        

	        statisticsMetrics.addQTimeSeriesStats(startTimeFormatted, endTimeFormatted, queueStatsMap);
	        // TODO: logging the time series data to the log files
	        
	    } catch (RuntimeException e) {
	        System.out.println("Error occurred while parsing statistics PCFMessage: " + e.getMessage());
	        e.printStackTrace();
	        PCFParser.parsePCFMessage(pcfMsg);
	    }
	}
	
	// returns a queue stats map for single queue. e.g., {QName: {"PUTS":1,"GETS":0,...))
    private Map<String, Map<String, Integer>> processQueue(Enumeration<?> nestedParameters) {
    	Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();
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
                    QName = nestedParameter.getStringValue().trim();
                    
                    // If queue name contains the unwanted patterns, return an empty map
                    if(QName.contains("ADMIN") || QName.contains("SYSTEM") || QName.contains("AMQ")) {
                        log.info("Skipping stats info for " + QName);
                        return queueStatsMap;
                    }
                    break;
                // once again summing persistent and non-persistent messages, see link for details:
                // https://www.ibm.com/docs/en/ibm-mq/7.5?topic=reference-notes#q037510___q037510_2
                case MQConstants.MQIAMO_PUTS:
                case MQConstants.MQIAMO_PUT1S:
                    if (value instanceof int[]) {
                    	int sum = Arrays.stream((int[]) value).sum();
                        quantPut = quantPut + sum;
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
                        int sum = Arrays.stream((int[]) value).sum();
                        quantGet = quantGet + sum;
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
        if(QName != null) {
        	log.info("Adding stats info for " + QName);
            observedQueues.add(QName);
            Map<String, Integer> statsForQueue = new HashMap<>();
            statsForQueue.put("PUTS", quantPut);
            statsForQueue.put("PUTS_FAILED", quantPutFails);
            statsForQueue.put("GETS", quantGet);
            statsForQueue.put("GETS_FAILED", quantGetFails);
            queueStatsMap.put(QName, statsForQueue);
            
        }
        return queueStatsMap;
    }
    
}
