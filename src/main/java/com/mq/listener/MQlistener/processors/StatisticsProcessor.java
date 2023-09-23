package com.mq.listener.MQlistener.processors;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.MQCFGR;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.config.Config.QMConfig;
import com.mq.listener.MQlistener.logging.StatisticsLogger;
import com.mq.listener.MQlistener.models.Issue.ActivitySpike;
import com.mq.listener.MQlistener.parsers.PCFParser;
import com.mq.listener.MQlistener.utils.IssueSender;

@Component
public class StatisticsProcessor {
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH.mm.ss");
    private static final Logger log = LoggerFactory.getLogger(StatisticsProcessor.class);
    
    private final ConfigManager configManager;
    int queueManagerMaxConnections;
    int queueManagerMaxOperations;
    
    
    @Autowired
    public StatisticsProcessor(ConfigManager configManager) {
        this.configManager = configManager;

    }
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;
    
    
    // observedQueues stores queues from past messages, STATQ messages only include any one given queue if there's been some
    // operation on it (e.g. put, get,...). observedQueues allows us to interpolate 0's data where we don't recieve any data 
    // for any given queue
    
    // queueStatsMap records data from a single STATQ message (ie one period), it includes puts, gets,...
    
    // timeSeriesStats is a linked hashmap (allowing for chronology), where the key is the date-time of the retreval of 
    // a STATQ message and it contains a time-series of queueStatsMap's
    
    private static final Set<String> observedQueues = new HashSet<>();
    private static final Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();
    
    private static final Map<Map.Entry<LocalTime, LocalTime>, Map<String, Map<String, Integer>>> timeSeriesStats = new LinkedHashMap<>();
    
    // having different timeseries for whole QM because it comes at different time intervals
    private static final Map<Map.Entry<LocalTime, LocalTime>, Map<String, Integer>> QMTimeSeriesStats = new LinkedHashMap<>();

    
    private static Map<String, ActivitySpike> issueObjectMap = new HashMap<>();
    @Autowired
    private IssueSender sender;
	@Autowired
	private StatisticsLogger logger;
    
    public void processStatisticsMessage(PCFMessage pcfMsg) throws Exception {
    	if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return;
        }

		String commandString = PCFParser.extractCommandString(pcfMsg);
//		System.out.println("Recieved Stats message, command: " + commandString);
		
		switch (commandString) {
			case "MQCMD_STATISTICS_Q":
//				System.out.println("MQCMD_STATISTICS_Q");
				processStatQMessage(pcfMsg);
				break;
			case "MQCMD_STATISTICS_MQI":
//				System.out.println("MQCMD_STATISTICS_MQI");
//				PCFParser.parsePCFMessage(pcfMsg);
				processStatMQIMessage(pcfMsg);
				
				break;
			default:
				System.out.println("STATS MESSAGE OF UNKNOWN TYPE");
				break;
		}
    }
    
    
    public void processStatMQIMessage(PCFMessage pcfMsg) throws Exception {
    	
    	// getting queue manager name
    	String QMName = pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_MGR_NAME).trim();
    	
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
        Map.Entry<LocalTime, LocalTime> timeKey = new AbstractMap.SimpleEntry<>(startTimeFormatted, endTimeFormatted);
        String combinedTime = startTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss")) 
        		+ " - " 
        		+ endTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        QMTimeSeriesStats.put(timeKey, statsForQM);
        printTimeSeriesStatsQueueManager();
        checkQueueManagerActivity(timeKey, statsForQM, combinedTime);
        // logging to csv file the queue manager activity
        // TODO: better way to handle the difference between queue manager and queue logging
        logger.logToCsv(QMName,Optional.empty(), startTimeFormatted, endTimeFormatted, statsForQM);
        
    }
    
    public void processStatQMessage(PCFMessage pcfMsg) throws Exception {
    	// this is the type fthat we're dealing with: https://www.ibm.com/docs/en/ibm-mq/9.3?topic=reference-queue-statistics-message-data

        try {
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
            
            
            // get the time interval of the message
            String startTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim();
            LocalTime startTimeFormatted = LocalTime.parse(startTime, formatter);
            String endTime = pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim();
            LocalTime endTimeFormatted = LocalTime.parse(endTime, formatter);
            String combinedTime = startTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss")) 
            		+ " - " 
            		+ endTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            // check whether there is a spike of activity for any of the queues
            
            Map.Entry<LocalTime, LocalTime> timeKey = new AbstractMap.SimpleEntry<>(startTimeFormatted, endTimeFormatted);
            checkQueueActivity(timeKey, combinedTime);
            
            // now that all the data for this period is in queueStatsMap, we add it
            // to the timeSeriesStats, queueStatsMap is cleared each period because it's a static variable
            // and we're only interested in current data being in it
	        timeSeriesStats.put(timeKey, new HashMap<>(queueStatsMap));
	        ensureMaxEntries();
	        printTimeSeriesStatsQueues();
	        
	        // this loop logs the stats for each queue
	        for (Map.Entry<String, Map<String, Integer>> queue : queueStatsMap.entrySet()) {
	        	String queueName = queue.getKey();
	        	Map<String, Integer> queueStats = queue.getValue();
	        	// TODO: actually get the queue manager, via system vars
	        	logger.logToCsv("QM1", Optional.of(queueName), startTimeFormatted, endTimeFormatted, queueStats);
	        }
	        
            queueStatsMap.clear();
            
            // TODO: logging the time series data to the log files
            
        } catch (RuntimeException e) {
            System.out.println("Error occurred while parsing statistics PCFMessage: " + e.getMessage());
            e.printStackTrace();
            PCFParser.parsePCFMessage(pcfMsg);
        }
    }
    
    private void printTimeSeriesStatsQueues() {
        System.out.println("Time series stats for <QUEUES>:");
        timeSeriesStats.forEach((time, stats) -> {
            System.out.println("Time: " + time);
            if (stats.isEmpty()) {
                System.out.println("\tNo activity in period.");
                return;  // skip the rest of the processing for this time
            }
            stats.forEach((objectName, objectStats) -> {
                System.out.println("\tQueue name : " + objectName);
                objectStats.forEach((key, value) -> {
                    System.out.println("\t\t" + key + ": " + value);
                });
            });
        });
    }
    private void printTimeSeriesStatsQueueManager() {
        System.out.println("Time series stats for <QMGR>:");
        QMTimeSeriesStats.forEach((time, stats) -> {
            System.out.println("Time: " + time);
                stats.forEach((key, value) -> {
                    System.out.println("\t\t" + key + ": " + value);
                });
        });
    }
    
    private void checkQueueActivity(Map.Entry<LocalTime, LocalTime> timeKey, String combinedTime) throws Exception {
    	// so find rate per minute of put/gets and handle the logic
        long intervalInSeconds = java.time.Duration.between(timeKey.getKey(), timeKey.getValue()).getSeconds();
        if (intervalInSeconds <= 0) {
            log.warn("Invalid time interval.");
            return;
        }
        // for each queue entry
        for (Map.Entry<String, Map<String, Integer>> queueEntry : queueStatsMap.entrySet()) {
            String queueName = queueEntry.getKey();
            Map<String, Integer> stats = queueEntry.getValue();
            
            int requests = stats.getOrDefault("PUTS", 0)
            		+ stats.getOrDefault("PUTS_FAILED", 0)
            		+ stats.getOrDefault("GETS", 0)
            		+ stats.getOrDefault("GETS_FAILED", 0);
            double requestRatePerMinute = (60.0 * requests) / intervalInSeconds;
            // use either the default threshold or specialised threshold
            if (requestRatePerMinute > getQueueMaxOperations(queueName)) {
                String message = 
                "Spike in MQ operations on: " 
                + queueName 
                + ", which has a configured threshold of no more than "
                + getQueueMaxOperations(queueName)
                + " operations";
                ActivitySpike issue = issueObjectMap.getOrDefault(queueName, new ActivitySpike(
                		message,
                		"<QUEUE>", 
                		queueName));
            	Map<String, String> detailsHashMap = new HashMap<>();
            	detailsHashMap.put("requestRate", Double.toString(requestRatePerMinute));
            	issue.addWindowData(detailsHashMap, combinedTime);
            	
            	// now that the issue is created/ updated, send it to frontend
                sender.sendIssue(issue);
                // update issue map
                issueObjectMap.put(queueName, issue);
            }
       }
    }
    private void checkQueueManagerActivity( Map.Entry<LocalTime, LocalTime> timeKey, Map<String, Integer> stats, String combinedTime) throws Exception {
    	// load specific queue manger settings
    	QMConfig queueManagerConfig = 
    	configManager
    	.getConfig()
    	.getQms()
    	.getOrDefault(
    			qMgrName, 
    			configManager.getConfig().getQms().get("<DEFAULT>"));
    	
    	queueManagerMaxConnections = 
    	queueManagerConfig
    	.getQueueManager()
    	.getConnections()
    	.getMax();
    	queueManagerMaxOperations = 
    	queueManagerConfig
    	.getQueueManager()
		.getOperations()
		.getMax();

    	System.out.println(
    	"queueManagerMaxConnections: " 
    	+ queueManagerMaxConnections
    	+ " queueManagerMaxOperations: " 
    	+ queueManagerMaxOperations);
        // a flag to tell whether issue is present for queue in this window
    	Boolean flag = false;
    	String message = "";
    	// so find rate per minute of put/gets and handle the logic
        long intervalInSeconds = java.time.Duration.between(timeKey.getKey(), timeKey.getValue()).getSeconds();
        if (intervalInSeconds <= 0) {
            log.warn("Invalid time interval.");
            return;
        }
        int requests = stats.getOrDefault("PUTS", 0)
        		+ stats.getOrDefault("PUTS_FAILED", 0)
        		+ stats.getOrDefault("GETS", 0)
        		+ stats.getOrDefault("GETS_FAILED", 0);
        int conns = stats.getOrDefault("CONNS", 0) + stats.getOrDefault("CONNS_FAILED", 0);
        double requestRatePerMinute = (60.0 * requests) / intervalInSeconds;
        double connsRatePerMinute = (60.0 * conns) / intervalInSeconds;
        

        if (connsRatePerMinute > queueManagerMaxConnections) {
            message = 
            "Spike in connections to the queue manager " 
            + ", which has a configured threshold of no more than "
            + queueManagerMaxConnections
            + " connections per window";
            flag = true;
        } else if (requestRatePerMinute > queueManagerMaxOperations) {
            message = 
            "Spike in MQ operations on "
            + qMgrName
            + ", which has a configured threshold of no more than "
            + queueManagerMaxOperations
            + " operations per window";
            flag = true;
        }
        
        if (flag) {
            log.warn("Spike in activity detected for " + qMgrName + ": PutGetRate = {} per minute, ConnRate = {} per minute", 
            		requestRatePerMinute, connsRatePerMinute);

        	ActivitySpike issue = issueObjectMap.getOrDefault("<QMGR>", new ActivitySpike(
        		message,
	        	"<QMGR>",
	        	qMgrName));
        	Map<String, String> detailsHashMap = new HashMap<>();
        	detailsHashMap.put("requestRate", Double.toString(requestRatePerMinute));
        	detailsHashMap.put("connRate", Double.toString(connsRatePerMinute));
        	issue.addWindowData(detailsHashMap, combinedTime);
        	
        	// now that the issue is created/ updated, send it to frontend
            sender.sendIssue(issue);
            // update issue map
            issueObjectMap.put("<QMGR>", issue);
        }
    }
    
    /**
    When the size of timeSeriesStats gets to 30, this deletes the oldest entry, 
    to ensure not too much memory usage.
    */
    private void ensureMaxEntries() {
        while (timeSeriesStats.size() > 30) {
            // Creating an iterator for the keys of timeSeriesStats
            Iterator<Map.Entry<LocalTime, LocalTime>> i = timeSeriesStats.keySet().iterator();
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
                    QName = nestedParameter.getStringValue().trim();
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
        if(QName != null && !QName.contains("ADMIN") && !QName.contains("SYSTEM")&& !QName.contains("AMQ")) {
        	log.info("Adding stats info for " + QName);
            observedQueues.add(QName);
            Map<String, Integer> statsForQueue = new HashMap<>();
            statsForQueue.put("PUTS", quantPut);
            statsForQueue.put("PUTS_FAILED", quantPutFails);
            statsForQueue.put("GETS", quantGet);
            statsForQueue.put("GETS_FAILED", quantGetFails);
            queueStatsMap.put(QName, statsForQueue);
            
        }
    }
    
    /* gets the personalised maximum operations rate for a queue
     * or returns the default for this queue
     * */
    public int getQueueMaxOperations(String queueName) {
    	// load specific queue manger settings
    	QMConfig queueManagerConfig = 
    	configManager
    	.getConfig()
    	.getQms()
    	.getOrDefault(
    			qMgrName, 
    			configManager.getConfig().getQms().get("<DEFAULT>"));
    	
    	Map<String, Integer> operationsSpecificQueues = 
    	queueManagerConfig
    	.getQueue()
    	.getOperationsSpecificQueues();
    	// TODO: seems this might be run twice
    	Integer operationsDefault = 
    	    	queueManagerConfig
    	    	.getQueue()
    	    	.getOperationsDefault();
        return operationsSpecificQueues.getOrDefault(queueName, operationsDefault);
    }
}
