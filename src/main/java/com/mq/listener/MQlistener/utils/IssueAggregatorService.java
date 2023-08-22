package com.mq.listener.MQlistener.utils;

import org.springframework.stereotype.Service;

@Service
public class IssueAggregatorService {
    public void sendIssues(String jsonIssues) throws Exception {
    	
    	JsonUtil.writeToJsonFile(jsonIssues, "sample.json");
        System.out.println("Received Issues: " + jsonIssues);
    }
}