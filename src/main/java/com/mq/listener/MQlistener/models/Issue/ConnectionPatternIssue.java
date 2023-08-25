package com.mq.listener.MQlistener.models.Issue;


import TimeFormatter.TimeFormatter;


public class ConnectionPatternIssue extends Issue {
	    public ConnectionPatternIssue(int connectionCount,String generalDescription, double putGetCount, String userId) {
	        this.issueCode = "Missconfigured_Connection_Pattern";
	        this.startTimeStamp = TimeFormatter.formatNow();
	        this.generalDesc = generalDescription;
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
