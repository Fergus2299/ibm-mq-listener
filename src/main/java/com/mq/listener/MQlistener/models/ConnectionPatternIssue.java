//package com.mq.listener.MQlistener.models;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//
//
//public class ConnectionPatternIssue extends Issue {
//	  	private int connectionCount;
//	    private double putGetCount;
//		private String userId;
//
//	    public ConnectionPatternIssue(int connectionCount, double putGetCount, String userId) {
//	        this.issueCode = "Missconfigured_Connection_Pattern";
//	        this.startTimeStamp = formatNow();
//	        this.Q = "none";
//	        this.connectionCount = connectionCount;
//	        this.putGetCount = putGetCount;
//	        this.endTimestamp = null;
//	        this.userId = userId;
//	    }
//
//	    // getters and setters for connectionCount and putGetCount
//	    public int getConnectionCount() {
//	        return connectionCount;
//	    }
//
//	    public void setConnectionCount(int connectionCount) {
//	        this.connectionCount = connectionCount;
//	    }
//
//	    public double getputGetCount() {
//	        return putGetCount;
//	    }
//
//	    public void setputGetCount(double putGetCount) {
//	        this.putGetCount = putGetCount;
//	    }
//
//	    public String getUserId() {
//			return userId;
//		}
//
//		public void setUserId(String userId) {
//			this.userId = userId;
//		}
//
//		@Override
//	    public String toString() {
//	        return "ConnectionPatternError{" +
//	                "issueCode='" + issueCode + '\'' +
//	                ", startTimeStamp='" + startTimeStamp + '\'' +
//	                ", endTimestamp='" + endTimestamp + '\'' +
//	                ", Q='" + Q + '\'' +
//	                ", connectionCount=" + connectionCount +
//	                ", putGetCount=" + putGetCount +
//	                ", userId=" + userId +
//	                '}';
//	    }
//	    
//	    public String formatNow() {
//	        LocalDateTime now = LocalDateTime.now();
//	        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
//	        return now.format(formatter);
//	    } 
//	    
//	    public String getIssueReport() {
//	        StringBuilder report = new StringBuilder();
//
//	        report.append("Issue Code: ").append(this.issueCode).append("\n");
//	        report.append("Start Time: ").append(this.startTimeStamp).append("\n");
//	        if (endTimestamp != null) {
//	            report.append("End Time: ").append(this.endTimestamp).append("\n");
//	        }
//	        report.append("userId: ").append(this.userId).append("\n");
//
//	        return report.toString();
//	    }
//	    
//}
