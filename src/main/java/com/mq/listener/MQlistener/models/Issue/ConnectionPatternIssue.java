package com.mq.listener.MQlistener.models.Issue;


import java.util.HashMap;

import TimeFormatter.TimeFormatter;


public class ConnectionPatternIssue extends Issue {
	    public ConnectionPatternIssue(int connectionCount,String generalDescription, double putGetCount, String userId) {
	        this.issueCode = "Missconfigured_Connection_Pattern";
	        this.startTimeStamp = TimeFormatter.formatNow();
	        this.generalDesc = generalDescription;
	        this.technicalDetails = new HashMap<>();
	        this.MQObjectName = userId;
	    }
	    
	    // TODO: add addWindowData function
	    
	    

	    
}
