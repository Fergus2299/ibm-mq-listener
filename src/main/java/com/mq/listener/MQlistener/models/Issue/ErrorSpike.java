package com.mq.listener.MQlistener.models.Issue;


import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.mq.listener.MQlistener.models.Errors.ErrorDetails;
import com.mq.listener.MQlistener.utils.Utilities;

public class ErrorSpike extends Issue {

	
	DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	
    public ErrorSpike(String issueCode, String MQObjectType, String MQObjectName) {
        this.issueCode = issueCode;
        this.startTimeStamp = Utilities.formatNow();
        this.generalDesc = "";
        this.technicalDetails = new HashMap<>();
        this.MQObjectType = MQObjectType;
        this.MQObjectName = MQObjectName;
    }
    
    // puts window data into the map
    public void addWindowData(ErrorDetails details, Double rate) {
    	ArrayList<String> archivedRequestRates;
    	ArrayList<String> archivedTimestamps;
    	try {
    	    archivedRequestRates = (ArrayList<String>) technicalDetails.getOrDefault("archivedRequestRates", new ArrayList<String>());
    	    archivedTimestamps = (ArrayList<String>) technicalDetails.getOrDefault("archivedTimestamps", new ArrayList<String>());
    	} catch (ClassCastException e) {
    	    archivedRequestRates = new ArrayList<String>();
    	    archivedTimestamps = new ArrayList<String>();
    	    System.out.println("Error: The stored value for 'archivedRequestRates' or 'archivedTimestamps' was not of the expected type. Initializing with an empty list.");
    	}

        archivedRequestRates.add(rate.toString());
        String currentTimeString = Utilities.formatNow();
        archivedTimestamps.add(currentTimeString);

        technicalDetails.put("archivedRequestRates", archivedRequestRates);
        technicalDetails.put("archivedTimestamps", archivedTimestamps);
        
        Map<String, Object> detailsHashMap = details.toHashMap();
        detailsHashMap.remove("count");
        detailsHashMap.put("archivedRequestRates", archivedRequestRates); 
        detailsHashMap.put("archivedTimestamps", archivedTimestamps); 
        technicalDetails.putAll(detailsHashMap);
    }
}
