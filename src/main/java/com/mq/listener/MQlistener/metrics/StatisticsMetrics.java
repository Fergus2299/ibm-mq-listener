package com.mq.listener.MQlistener.metrics;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractMap;

import java.util.HashMap;
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
public class StatisticsMetrics {
    private static final Logger log = LoggerFactory.getLogger(StatisticsMetrics.class);
    
    private final ConfigManager configManager;
    int queueManagerMaxConnections;
    int queueManagerMaxOperations;
    
    
    @Autowired
    public StatisticsMetrics(ConfigManager configManager) {
        this.configManager = configManager;
        System.out.println("MyComponent has been initialized");
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
    
    
    private static final Map<Map.Entry<LocalTime, LocalTime>, Map<String, Map<String, Integer>>> QTimeSeries = new LinkedHashMap<>();
    
    // having different timeseries for whole QM because it comes at different time intervals
    private static final Map<Map.Entry<LocalTime, LocalTime>, Map<String, Integer>> QMTimeSeries = new LinkedHashMap<>();

    
    private static Map<String, ActivitySpike> issueObjectMap = new HashMap<>();
    @Autowired
    private IssueSender sender;
	@Autowired
	private StatisticsLogger logger;
    

    public void addQMTimeSeriesStats(
    		
    		LocalTime startTimeFormatted, 
    		LocalTime endTimeFormatted,
    		Map<String, Integer> statsForQM) {

        Map.Entry<LocalTime, LocalTime> timeKey = new AbstractMap.SimpleEntry<>(startTimeFormatted, endTimeFormatted);
        String combinedTime = startTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss")) 
        		+ " - " 
        		+ endTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    	
    	QMTimeSeries.put(timeKey, statsForQM);
    	printTimeSeriesStatsQueueManager();
    	
    	try {
			checkQueueManagerActivity(timeKey, statsForQM, combinedTime);
		} catch (Exception e) {
			System.out.println(e);
			log.warn(e.getMessage());
		}
    	logger.logToCsv(qMgrName,Optional.empty(), startTimeFormatted, endTimeFormatted, statsForQM);
    }
    
    
    
    public void addQTimeSeriesStats (
    		LocalTime startTimeFormatted, 
    		LocalTime endTimeFormatted,
    		Map<String, Map<String, Integer>> queueStatsMap) {
    	Map.Entry<LocalTime, LocalTime> timeKey = new AbstractMap.SimpleEntry<>(startTimeFormatted, endTimeFormatted);
		String combinedTime = startTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss")) 
			        		+ " - " 
			        		+ endTimeFormatted.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
		try {
			// check whether there is a spike of activity for any of the queues
			checkQueueActivity(timeKey, combinedTime, queueStatsMap);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e);
			log.warn(e.getMessage());
		}
        // now that all the data for this period is in queueStatsMap, we add it
        // and we're only interested in current data being in it
		QTimeSeries.put(timeKey, new HashMap<>(queueStatsMap));
		
        
        ensureMaxEntries();
        printTimeSeriesStatsQueues();
        
        // this loop logs the stats for each queue
        for (Map.Entry<String, Map<String, Integer>> queue : queueStatsMap.entrySet()) {
        	String queueName = queue.getKey();
        	Map<String, Integer> queueStats = queue.getValue();
        	// TODO: actually get the queue manager, via system vars
        	logger.logToCsv(qMgrName, Optional.of(queueName), startTimeFormatted, endTimeFormatted, queueStats);
        }
        
		
    }

    
    private void printTimeSeriesStatsQueues() {
        System.out.println("Time series stats for <QUEUES>:");
        QTimeSeries.forEach((time, stats) -> {
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
        QMTimeSeries.forEach((time, stats) -> {
            System.out.println("Time: " + time);
                stats.forEach((key, value) -> {
                    System.out.println("\t\t" + key + ": " + value);
                });
        });
    }
    
    private void checkQueueActivity(
    		Map.Entry<LocalTime, LocalTime> timeKey, 
    		String combinedTime,
    		Map<String, Map<String, Integer>> queueStatsMap) throws Exception {
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
    private void checkQueueManagerActivity( 
    		Map.Entry<LocalTime, LocalTime> timeKey, 
    		Map<String, Integer> stats, 
    		String combinedTime) throws Exception {
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
        while (QTimeSeries.size() > 30) {
            // Creating an iterator for the keys of timeSeriesStats
            Iterator<Map.Entry<LocalTime, LocalTime>> i = QTimeSeries.keySet().iterator();
            if (i.hasNext()) {
                i.next();
                i.remove();
            }
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
