package com.mq.listener.MQlistener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

// this is relevant to our application because a malicious internal user could 
// cause this issue
public class UnknownObjectNameIssue extends Issue {
    public UnknownObjectNameIssue(String Q) {
        this.issueCode = "UNKNOWN_OBJECT_NAME";
        this.startTimeStamp = formatNow();
        this.Q = Q;
    }
    
    
    public String formatNow() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return now.format(formatter);
    }
}
