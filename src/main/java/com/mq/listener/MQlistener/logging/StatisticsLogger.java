package com.mq.listener.MQlistener.logging;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.mq.listener.MQlistener.config.ConfigManager;

@Service
public class StatisticsLogger {
    private static final Logger logger = LoggerFactory.getLogger(StatisticsLogger.class);

	// TODO: ensure is atomic
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    protected static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    protected static final String BASE_PATH = "logs/";

    
    public void logToCsv(
    		
    		String QMName,
    		// queue name is an empty string if the stats are for the queue manager
    		Optional<String> optQName,
    		LocalTime startTime,
    		LocalTime endTime,
    		Map<String, Integer> statsForQM) {
    	System.out.println("Attempting to log stats data");
    	
    	LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.format(DATE_FORMATTER);
        String directoryPath;
        String logFilePath;
        
        // checking if file exists, if not it's created
        if (optQName.isPresent()) {
            directoryPath = BASE_PATH + QMName + "/Queues/";
            logFilePath = directoryPath + optQName.get() + "-" + currentDateString + ".csv";
        } else {
            directoryPath = BASE_PATH + QMName + "/QueueManager/";
            logFilePath = directoryPath + QMName + "-" + currentDateString + ".csv";
        }
        
        // Ensure the directory exists
        File logDir = new File(directoryPath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // Check if the file exists and, if not, create it
        File csvFile = new File(logFilePath);
        boolean isNewFile = false;
        if (!csvFile.exists()) {
            try {
                isNewFile = csvFile.createNewFile();
            } catch (IOException e) {
                logger.error("Error creating the log file.", e);
            }
        }
        
        
        
        // file lock and exclusive access ensured
        try (RandomAccessFile raf = new RandomAccessFile(csvFile, "rw");
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.lock()) {
        	
        	
            channel.position(channel.size());

            if (isNewFile) {
                raf.writeBytes("START_TIME,END_TIME,");
                for (String key : statsForQM.keySet()) {
                    raf.writeBytes(key + ",");
                }
                raf.writeBytes(System.lineSeparator());
            }

            StringBuilder line = new StringBuilder();
            line.append(startTime.format(TIME_FORMATTER)).append(",");
            line.append(endTime.format(TIME_FORMATTER)).append(",");
            for (Map.Entry<String, Integer> entry : statsForQM.entrySet()) {
                line.append(entry.getValue()).append(",");
            }
            line.setLength(line.length() - 1);
            line.append(System.lineSeparator());
            
            raf.writeBytes(line.toString());
        } catch (IOException e) {
            logger.error("Error writing to CSV", e);
        } 
        
        
               
    }
}
