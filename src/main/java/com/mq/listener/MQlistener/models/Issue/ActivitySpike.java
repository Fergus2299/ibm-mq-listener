package com.mq.listener.MQlistener.models.Issue;

import TimeFormatter.TimeFormatter;

public class ActivitySpike extends Issue{
    public ActivitySpike(String MQObjectType, 
    		String MQObjectName) {
        this.issueCode = "Too_Much_Activity";
        this.startTimeStamp = TimeFormatter.formatNow();
        this.generalDesc = "";
        this.technicalDetails = "";
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
    }
}
