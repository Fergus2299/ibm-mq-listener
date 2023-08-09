package com.mq.listener.MQlistener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TooMany2035sIssue extends Issue {
    
    private int requestCount;

    public TooMany2035sIssue(String queueName) {
        this.issueCode = "Too_Many_2035s";
        this.startTimeStamp = formatNow();
        this.Q = queueName;
        this.requestCount = 0;
    }

    public void incrementRequestCount() {
        this.requestCount++;
    }

    public int getRequestCount() {
        return this.requestCount;
    }

    private String formatNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return now.format(formatter);
    }
    public void incrementRequestCountBy(int count) {
        this.requestCount += count;
    }
}