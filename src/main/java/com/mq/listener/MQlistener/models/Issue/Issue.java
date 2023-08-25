package com.mq.listener.MQlistener.models.Issue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import TimeFormatter.TimeFormatter;


public abstract class Issue {
    protected String issueCode;
    protected String startTimeStamp;
    protected String generalDesc;
    protected String technicalDetails;
    protected String MQObjectType; // in {queue, channel, app, queueManager}
    protected String MQObjectName;
    


    
    
    public String getIssueCode() {
		return issueCode;
	}
	public void setIssueCode(String issueCode) {
		this.issueCode = issueCode;
	}
	public String getStartTimeStamp() {
		return startTimeStamp;
	}
	public void setStartTimeStamp(String startTimeStamp) {
		this.startTimeStamp = startTimeStamp;
	}

	public String getGeneralDesc() {
		return generalDesc;
	}
	public void setGeneralDesc(String generalDesc) {
		this.generalDesc = generalDesc;
	}
	public String getTechnicalDetails() {
		return technicalDetails;
	}
	public void setTechnicalDetails(String technicalDetails) {
		this.technicalDetails = technicalDetails;
	}
	public String getMQObjectType() {
		return MQObjectType;
	}
	public void setMQObjectType(String mQObjectType) {
		MQObjectType = mQObjectType;
	}
	public String getMQObjectName() {
		return MQObjectName;
	}
	public void setMQObjectName(String mQObjectName) {
		MQObjectName = mQObjectName;
	}
	
    public void printIssueDetails() {
        System.out.println("----------- Issue Details -----------");
        System.out.println("Issue Code: " + this.issueCode);
        System.out.println("Start Timestamp: " + this.startTimeStamp);
        

        
        System.out.println("MQ Object Type: " + this.MQObjectType);
        System.out.println("MQ Object Name: " + this.MQObjectName);
        System.out.println("General Description: " + this.generalDesc);
        System.out.println("Technical Details: " + this.technicalDetails);
    }
	
    public void closeIssue() {
    }

}