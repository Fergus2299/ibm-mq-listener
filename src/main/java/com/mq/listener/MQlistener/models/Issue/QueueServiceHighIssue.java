package com.mq.listener.MQlistener.models.Issue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import TimeFormatter.TimeFormatter;

public class QueueServiceHighIssue extends Issue {
	
	// constructor function
	public QueueServiceHighIssue(String QName, Integer timeSinceReset, Integer highQDepth, Integer enQCount, Integer deQCount) {
        this.issueCode = "Queue_Service_High";
        this.startTimeStamp = TimeFormatter.formatNow();
        this.generalDesc = 
        		"Queue: "
        		+ QName
        		+ " produced a service high event at:  "
        		+ this.startTimeStamp;
        this.technicalDetails = "Here is where we will have the other info!";
        this.MQObjectType = "<QUEUE>";
        this.MQObjectName = QName;
    }
	
	
	
	
	

    
}