package com.mq.listener.MQlistener;

public class AccountingData {

    private String userIdentifier;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private Integer puts;
    private Integer putsFailed;
    private Integer put1s;
    private Integer put1sFailed;
    private Integer gets;
    private Integer getsFailed;
	public String getUserIdentifier() {
		return userIdentifier;
	}
	public void setUserIdentifier(String userIdentifier) {
		this.userIdentifier = userIdentifier;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	public Integer getPuts() {
		return puts;
	}
	public void setPuts(Integer putsValues) {
		this.puts = putsValues;
	}
	public Integer getPutsFailed() {
		return putsFailed;
	}
	public void setPutsFailed(Integer putsFailed) {
		this.putsFailed = putsFailed;
	}
	public Integer getPut1s() {
		return put1s;
	}
	public void setPut1s(Integer put1sValues) {
		this.put1s = put1sValues;
	}
	public Integer getPut1sFailed() {
		return put1sFailed;
	}
	public void setPut1sFailed(Integer put1sFailed) {
		this.put1sFailed = put1sFailed;
	}
	public Integer getGets() {
		return gets;
	}
	public void setGets(Integer getsValues) {
		this.gets = getsValues;
	}
	public Integer getGetsFailed() {
		return getsFailed;
	}
	public void setGetsFailed(Integer getsFailed) {
		this.getsFailed = getsFailed;
	}
    
    // a method for console logging the object
    @Override
    public String toString() {
        return "AccountingData{" +
                "userIdentifier='" + userIdentifier + '\'' +
                ", startDate='" + startDate + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endDate='" + endDate + '\'' +
                ", endTime='" + endTime + '\'' +
                ", puts=" + puts +
                ", putsFailed=" + putsFailed +
                ", put1s=" + put1s +
                ", put1sFailed=" + put1sFailed +
                ", gets=" + gets +
                ", getsFailed=" + getsFailed +
                '}';
    }
    
    

}