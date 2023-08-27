package com.mq.listener.MQlistener.models.Issue;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
	    public void addWindowData(Map<String, String> detailsHashMap,  Map.Entry<LocalTime, LocalTime> timeKey) {
	    	ArrayList<String> archivedconns;
	    	ArrayList<String> archivedputGetCount;
	    	ArrayList<String> archiveduserRatio;
	    	ArrayList<Map.Entry<LocalTime, LocalTime>> archivedlogTimes;
	    	
	    try {
	    	archivedconns = (ArrayList<String>) technicalDetails.getOrDefault("archivedconns", new ArrayList<String>());
	    	archivedputGetCount = (ArrayList<String>) technicalDetails.getOrDefault("archivedputGetCount", new ArrayList<String>());
	    	archiveduserRatio = (ArrayList<String>) technicalDetails.getOrDefault("archiveduserRatio", new ArrayList<String>());
	    	archivedlogTimes = (ArrayList<Map.Entry<LocalTime, LocalTime>>) technicalDetails.getOrDefault("archivedlogTimes", new ArrayList<Map.Entry<LocalTime, LocalTime>>());
	    } catch (ClassCastException e) {
	    	archivedconns = new ArrayList<String>();
	    	archivedputGetCount = new ArrayList<String>();
	    	archiveduserRatio = new ArrayList<String>();
	    	archivedlogTimes = new ArrayList<Map.Entry<LocalTime, LocalTime>>();
    	    System.out.println("Error: The stored value for 'archievedRate' or 'archievedTimestamps' was not of the expected type. Initializing with an empty list.");
    	}
	    
    	Map<String, Object> newHashmap = new HashMap<>();
    	archivedconns.add(detailsHashMap.get("conns"));
        newHashmap.put("archivedconns", archivedconns);
        
        archivedputGetCount.add(detailsHashMap.get("putGetCount"));
        newHashmap.put("archivedputGetCount", archivedputGetCount);
        
        archiveduserRatio.add(detailsHashMap.get("userRatio"));
        newHashmap.put("archiveduserRatio", archiveduserRatio);
        
        archivedlogTimes.add(timeKey);
        newHashmap.put("archivedlogTimes", archivedlogTimes);
        
        technicalDetails.putAll(newHashmap);

	    	

	    }

}
