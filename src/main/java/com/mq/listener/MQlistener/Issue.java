package com.mq.listener.MQlistener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public abstract class Issue {
    protected String issueCode;
    protected String startTimeStamp;
    protected String endTimestamp; // this could be null if the issue has not ended
    protected String Q;
    
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
	public String getQ() {
		return Q;
	}
	public void setQ(String q) {
		Q = q;
	}



}