package com.mq.listener.MQlistener.models;


import TimeFormatter.TimeFormatter;


public class ConnectionPatternIssue extends Issue {
	    public ConnectionPatternIssue(int connectionCount, double putGetCount, String userId) {
	        this.issueCode = "Missconfigured_Connection_Pattern";
	        this.startTimeStamp = TimeFormatter.formatNow();
	        this.endTimestamp = null;
	        this.generalDesc = "";
	        this.technicalDetails = 
	        		"{\"connectionCount\": " 
			        + connectionCount 
			        + ", \"putGetCount\": " 
			        + putGetCount + "}";
	        
	        this.MQObjectType = "<APP>";
	        this.MQObjectName = userId;
	    }
	    
	    // TODO: add addWindowData function
	    
	    
	    
}
