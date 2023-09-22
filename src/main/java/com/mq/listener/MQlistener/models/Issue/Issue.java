package com.mq.listener.MQlistener.models.Issue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public abstract class Issue {
    private static final String BASE_PATH = "logs/issues/";
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Logger logger = LoggerFactory.getLogger(Issue.class);
    
    protected String issueCode;
    protected String startTimeStamp;
    protected String generalDesc;
    protected Map<String, Object> technicalDetails;
    protected String MQObjectType; // in {queue, channel, app, queueManager}
    protected String MQObjectName;
    
    public Issue() {
        // we log to csv
    	System.out.println("Creating issue");
    }

    public String getIssueCode() {
		return issueCode;
	}
	public void setIssueCode(String issueCode) {
		this.issueCode = issueCode;
	}
	public String getStartTimeStamp() {
		return startTimeStamp;
	}
	public void setStartTimeStamp(String startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public String getGeneralDesc() {
		return generalDesc;
	}
	public void setGeneralDesc(String generalDesc) {
		this.generalDesc = generalDesc;
	}
	public String getMQObjectType() {
		return MQObjectType;
	}
	public void setMQObjectType(String mQObjectType) {
		MQObjectType = mQObjectType;
	}
	public String getMQObjectName() {
		return MQObjectName;
	}
	public void setMQObjectName(String mQObjectName) {
		MQObjectName = mQObjectName;
	}
    public Map<String, Object> getTechnicalDetails() {
		return technicalDetails;
	}
	public void setTechnicalDetails(Map<String, Object> technicalDetails) {
		this.technicalDetails = technicalDetails;
	}
	
	
	public void printIssueDetails() {
	    System.out.println("----------- Issue Details -----------");
	    System.out.println("Issue Code: " + this.issueCode);
	    System.out.println("Start Timestamp: " + this.startTimeStamp);
	    System.out.println("MQ Object Type: " + this.MQObjectType);
	    System.out.println("MQ Object Name: " + this.MQObjectName);
	    System.out.println("General Description: " + this.generalDesc);

	    // Print technical details if they are not null or empty
	    if (technicalDetails != null && !technicalDetails.isEmpty()) {
	        System.out.println("---- Technical Details ----");
	        for (Map.Entry<String, Object> entry : technicalDetails.entrySet()) {
	            System.out.println(entry.getKey() + ": " + entry.getValue());
	        }
	    }
	}
	
    public void logToJson(){
    	
    	String logFilePath;

        // Ensure the directory exists
        File logDir = new File(BASE_PATH);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    	// getting file directory
    	LocalDate currentDate = LocalDate.now();
    	String currentDateString = currentDate.format(DATE_FORMATTER);
    	logFilePath = BASE_PATH + "issues - " + currentDateString+ ".csv";
        
    	
    	// Check if the file exists and, if not, create it
        File jsonFile = new File(logFilePath);
        boolean isNewFile = false;
        if (!jsonFile.exists()) {
            try {
                isNewFile = jsonFile.createNewFile();
            } catch (IOException e) {
                logger.error("Error creating the log file.", e);
            }
        }
        try (RandomAccessFile raf = new RandomAccessFile(jsonFile, "rw");
                FileChannel channel = raf.getChannel();
                FileLock lock = channel.lock()) {
        	channel.position(channel.size());
            if (isNewFile) {
                raf.writeBytes("issueCode,startTimeStamp,MQObjectType,MQObjectName" + System.lineSeparator());
            }
            StringBuilder line = new StringBuilder();
            line.append(this.getIssueCode()).append(",");
            line.append(this.getStartTimeStamp()).append(",");
            line.append(this.getMQObjectType()).append(",");
            line.append(this.getMQObjectName()).append(",");
            line.setLength(line.length() - 1);
            line.append(System.lineSeparator());
            raf.writeBytes(line.toString());
        } catch (IOException e) {
            logger.error("Error writing to CSV", e);
        } 
        
        
    }

}