package com.mq.listener.MQlistener.models.Issue;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;

import com.mq.listener.MQlistener.utils.Utilities;

public class QueueServiceHighIssue extends Issue {

	// constructor function
	public QueueServiceHighIssue(String QName, Integer timeSinceReset, Integer highQDepth, Integer enQCount, Integer deQCount) {
		 double rate = (deQCount != 0) ? (double) enQCount / deQCount : 0;
        this.issueCode = "Queue_Service_High";
        this.startTimeStamp = Utilities.formatNow();
        this.generalDesc = 
                "Queue: "
                + QName
                + " produced a service high event at:  "
                + Utilities.prettyDateTime()
                + ", at which point "
                + String.format("%.2f", rate) // rounded to 2dp
                + " messages were put for each message retrieved"
                ;
        // issue active when service high event created
        this.technicalDetails = new HashMap<>();
        this.technicalDetails.put("isActiveIssue", "1"); 
        this.technicalDetails.put("timeSinceReset", timeSinceReset.toString());
        this.technicalDetails.put("highQDepth", highQDepth.toString());
        this.technicalDetails.put("enQCount", enQCount.toString());
        this.technicalDetails.put("deQCount", deQCount.toString());
        this.MQObjectType = "<QUEUE>";
        this.MQObjectName = QName;
        logToCsv();
    }
	
	public void okEventReceived() {
		// ensure that 
        this.generalDesc += "; Queue interval is now ok as of: " + Utilities.prettyDateTime() + ".";
        this.technicalDetails.put("isActiveIssue", "0");
    }
}
