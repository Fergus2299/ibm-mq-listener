package com.mq.listener.MQlistener.models.Issue;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mq.listener.MQlistener.models.Errors.ErrorDetails;

import TimeFormatter.TimeFormatter;

public class ErrorSpike extends Issue {
    public ErrorSpike(String issueCode, String MQObjectType, String MQObjectName) {
        this.issueCode = issueCode;
        this.startTimeStamp = TimeFormatter.formatNow();
        this.generalDesc = "";
        this.technicalDetails = new HashMap<>();
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
    }
    
    // puts window data into the map
    public void addWindowData(ErrorDetails details, Double rate) {

    	
    	ArrayList<String> archivedRates;
    	ArrayList<String> archivedTimestamps;
    	try {
    	    archivedRates = (ArrayList<String>) technicalDetails.getOrDefault("archievedRate", new ArrayList<String>());
    	    archivedTimestamps = (ArrayList<String>) technicalDetails.getOrDefault("archievedTimestamps", new ArrayList<String>());
    	} catch (ClassCastException e) {
    	    archivedRates = new ArrayList<String>();
    	    archivedTimestamps = new ArrayList<String>();
    	    System.out.println("Error: The stored value for 'archievedRate' or 'archievedTimestamps' was not of the expected type. Initializing with an empty list.");
    	}

        archivedRates.add(rate.toString());  // Convert the Double to String
        archivedTimestamps.add(TimeFormatter.formatNow());  // Store the current timestamp

        technicalDetails.put("archievedRate", archivedRates);
        technicalDetails.put("archievedTimestamps", archivedTimestamps);
        
        Map<String, Object> detailsHashMap = details.toHashMap();
        detailsHashMap.put("archievedRate", archivedRates); 
        detailsHashMap.put("archievedTimestamps", archivedTimestamps); 
        technicalDetails.putAll(detailsHashMap);
    }
}