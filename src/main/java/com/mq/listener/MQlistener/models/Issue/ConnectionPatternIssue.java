package com.mq.listener.MQlistener.models.Issue;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.mq.listener.MQlistener.utils.Utilities;


public class ConnectionPatternIssue extends Issue {
	@Autowired
	Utilities utilities;
	    public ConnectionPatternIssue(int connectionCount,String generalDescription, double putGetCount, String userId) {
	        this.issueCode = "Misconfigured_Connection_Pattern";
	        this.startTimeStamp = utilities.formatNow();
	        this.generalDesc = generalDescription;
	        this.technicalDetails = new HashMap<>();
	        this.MQObjectName = userId;
	    }
	    // TODO: add addWindowData function
	    public void addWindowData(Map<String, String> detailsHashMap,  String timeKey) {
	    	ArrayList<String> archivedconns;
	    	ArrayList<String> archivedRequestCount;
	    	ArrayList<String> archiveduserRatio;
	    	ArrayList<String> archivedlogTimes;
	    	
		    try {
		    	archivedconns = (ArrayList<String>) technicalDetails.getOrDefault("archivedconns", new ArrayList<String>());
		    	archivedRequestCount = (ArrayList<String>) technicalDetails.getOrDefault("archivedRequestCount", new ArrayList<String>());
		    	archiveduserRatio = (ArrayList<String>) technicalDetails.getOrDefault("archiveduserRatio", new ArrayList<String>());
		    	archivedlogTimes = (ArrayList<String>) technicalDetails.getOrDefault("archivedlogTimes", new ArrayList<String>());
		    } catch (ClassCastException e) {
		    	archivedconns = new ArrayList<String>();
		    	archivedRequestCount = new ArrayList<String>();
		    	archiveduserRatio = new ArrayList<String>();
		    	archivedlogTimes = new ArrayList<String>();
	    	    System.out.println("Error: The stored value for 'archivedRequestCount' or 'archievedTimestamps' was not of the expected type. Initializing with an empty list.");
	    	}
		    
	    	Map<String, Object> newHashmap = new HashMap<>();
	    	archivedconns.add(detailsHashMap.get("conns"));
	        newHashmap.put("archivedconns", archivedconns);
	        
	        archivedRequestCount.add(detailsHashMap.get("putGetCount"));
	        newHashmap.put("archivedRequestCount", archivedRequestCount);
	        
	        archiveduserRatio.add(detailsHashMap.get("userRatio"));
	        newHashmap.put("archiveduserRatio", archiveduserRatio);
	        
	        archivedlogTimes.add(timeKey);
	        newHashmap.put("archivedlogTimes", archivedlogTimes);
	        
	        technicalDetails.putAll(newHashmap);
        }

}
