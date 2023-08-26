package com.mq.listener.MQlistener.models.Issue;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mq.listener.MQlistener.models.Errors.ErrorDetails;

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
    // puts window data into the map
    public void addWindowData(Map<String, String> detailsHashMap, Map.Entry<LocalTime, LocalTime> timeKey) {
    	ArrayList<String> archivedRequestRates;
    	ArrayList<String> archivedConnRates;
    	ArrayList<Map.Entry<LocalTime, LocalTime>> archivedTimestamps;
    	// just for queue manager
    	try {
    	    archivedRequestRates = (ArrayList<String>) technicalDetails.getOrDefault("archivedRequestRates", new ArrayList<String>());
    	    archivedConnRates = (ArrayList<String>) technicalDetails.getOrDefault("archivedConnRates", new ArrayList<String>());
    	    archivedTimestamps = (ArrayList<Map.Entry<LocalTime, LocalTime>>) technicalDetails.getOrDefault("archievedTimestamps", new ArrayList<Map.Entry<LocalTime, LocalTime>>());
    	} catch (ClassCastException e) {
    		archivedConnRates = new ArrayList<String>();
    		archivedRequestRates = new ArrayList<String>();
    	    archivedTimestamps = new ArrayList<Map.Entry<LocalTime, LocalTime>>();
    	    System.out.println("Error: The stored value for 'archievedRate' or 'archievedTimestamps' was not of the expected type. Initializing with an empty list.");
    	}
    	
    	// the new data
    	Map<String, Object> newHashmap = new HashMap<>();
    	archivedRequestRates.add(detailsHashMap.get("requestRate"));
        newHashmap.put("archivedRequestRates", archivedRequestRates); 

        // only adding connName if QM issue, this logic is handled in parent function
    	if (detailsHashMap.get("connRate") != null) {
    		archivedConnRates.add(detailsHashMap.get("connRate"));
            newHashmap.put("archivedConnRates", archivedConnRates); 

    	}
        archivedTimestamps.add(timeKey);
        newHashmap.put("archievedTimestamps", archivedTimestamps); 
        
        technicalDetails.putAll(newHashmap);
    }
}
