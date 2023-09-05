package com.mq.listener.MQlistener.logging;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

@Service
public class BaseLogger {
	// TODO: ensure that user opening the log files while the app is running will not break the app
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
                e.printStackTrace();
            }
        }
        
        // adding to the file
    	try (FileWriter csvWriter = new FileWriter(logFilePath, true)) {
    		// if a new file then make the column names
            if (isNewFile) {
                csvWriter.append("START_TIME,END_TIME,");
                for (String key : statsForQM.keySet()) {
                    csvWriter.append(key).append(",");
                }
                csvWriter.append("\n");
            }

    		// now making the string
            StringBuilder line = new StringBuilder();
            System.out.println("startTime: " + startTime);
            System.out.println("endTime: " + endTime);
            line.append(startTime.format(TIME_FORMATTER)).append(",");
            line.append(endTime.format(TIME_FORMATTER)).append(",");
            // each entry in statsForQM
            for (Map.Entry<String, Integer> entry : statsForQM.entrySet()) {
                line.append(entry.getValue()).append(",");
            }
            // getting rid of final comma and adding new line
            line.setLength(line.length() - 1);
            line.append("\n");

            csvWriter.append(line.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
}
