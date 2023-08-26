package com.mq.listener.MQlistener.models.Issue;

import java.util.HashMap;

import TimeFormatter.TimeFormatter;

public class ActivitySpike extends Issue{
    public ActivitySpike(String MQObjectType, 
    		String MQObjectName
    		
    		) {
        this.issueCode = "Too_Much_Activity";
        this.startTimeStamp = TimeFormatter.formatNow();
        this.generalDesc = "";
        this.technicalDetails = new HashMap<>();
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
    }
}
