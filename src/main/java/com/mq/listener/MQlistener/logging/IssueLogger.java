//package com.mq.listener.MQlistener.logging;
//
//
//
//import java.io.File;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.channels.FileChannel;
//import java.nio.channels.FileLock;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mq.listener.MQlistener.models.Issue.Issue;
//
//public class IssueLogger {
//    private static final Logger logger = LoggerFactory.getLogger(IssueLogger.class);
//    private static final String BASE_PATH = "logs/issues/";
//    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//    private static final ObjectMapper mapper = new ObjectMapper();
//    
//    
//    public void logToJson(Issue issue){
//    	
//    	String logFilePath;
//
//        // Ensure the directory exists
//        File logDir = new File(BASE_PATH);
//        if (!logDir.exists()) {
//            logDir.mkdirs();
//        }
//    	// getting file directory
//    	LocalDate currentDate = LocalDate.now();
//    	String currentDateString = currentDate.format(DATE_FORMATTER);
//    	logFilePath = BASE_PATH + "issues - " + currentDateString+ ".json";
//        
//    	
//    	// Check if the file exists and, if not, create it
//        File jsonFile = new File(logFilePath);
//        boolean isNewFile = false;
//        if (!jsonFile.exists()) {
//            try {
//                isNewFile = jsonFile.createNewFile();
//            } catch (IOException e) {
//                logger.error("Error creating the log file.", e);
//            }
//        }
//        try (RandomAccessFile raf = new RandomAccessFile(jsonFile, "rw");
//                FileChannel channel = raf.getChannel();
//                FileLock lock = channel.lock()) {
//        	channel.position(channel.size());
//            if (isNewFile) {
//                raf.writeBytes("issueCode,startTimeStamp,MQObjectType,MQObjectName,");
//            }
//            StringBuilder line = new StringBuilder();
//            line.append(issue.getIssueCode()).append(",");
//            line.append(issue.getStartTimeStamp()).append(",");
//            line.append(issue.getMQObjectType()).append(",");
//            line.append(issue.getMQObjectName()).append(",");
//            line.setLength(line.length() - 1);
//            line.append("\n");
//            raf.writeBytes(line.toString());
//        } catch (IOException e) {
//            logger.error("Error writing to CSV", e);
//        } 
//        
//        
//    }
//}
