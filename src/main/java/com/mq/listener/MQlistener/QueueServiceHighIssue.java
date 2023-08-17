package com.mq.listener.MQlistener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueueServiceHighIssue extends Issue {
	
	// constructor function
	public QueueServiceHighIssue(String Q) {
        this.issueCode = "Queue_Service_High";
        this.startTimeStamp = formatNow();
        this.Q = Q;
        this.endTimestamp = null;
    }
	
	
	
	
	
    public String formatNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return now.format(formatter);
    }
    
}
