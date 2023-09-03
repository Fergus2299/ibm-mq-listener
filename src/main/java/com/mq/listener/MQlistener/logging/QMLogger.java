package com.mq.listener.MQlistener.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class QMLogger {
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final String BASE_PATH = "logs/";
    
    
    public void logToCsv(String QMName, 
    		LocalTime startTime, 
    		LocalTime endTime, 
    		Map<String, Integer> statsForQM) {
        LocalDate currentDate = LocalDate.now();
        String currentDateString = currentDate.format(DATE_FORMATTER);
    	
        // creating correct directory if it doesn't exist already
        String logFilePath = BASE_PATH + QMName + "/QueueManager/" + QMName + "-" + currentDateString + ".csv";
        File logDir = new File(BASE_PATH + QMName);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        
        
        // checking if file exists and if not, create it
    	File csvFile = new File(logFilePath);
        boolean isNewFile = false;

        if (!csvFile.exists()) {
            try {
                isNewFile = csvFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    	
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
