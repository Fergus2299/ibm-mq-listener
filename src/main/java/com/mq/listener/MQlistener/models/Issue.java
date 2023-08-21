package com.mq.listener.MQlistener.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public abstract class Issue {
    protected String issueCode;
    protected String startTimeStamp;
    protected String endTimestamp; // this could be null if the issue has not ended
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
	public String getEndTimestamp() {
		return endTimestamp;
	}
	public void setEndTimestamp(String endTimestamp) {
		this.endTimestamp = endTimestamp;
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

}