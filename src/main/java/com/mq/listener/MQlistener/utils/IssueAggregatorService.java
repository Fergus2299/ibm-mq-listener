package com.mq.listener.MQlistener.utils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class IssueAggregatorService {
    private static final Map<String, Object> aggregatedIssues = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public void sendIssues(String type, Object issues) throws Exception {
        // Update the aggregated issues map based on type
        aggregatedIssues.put(type, issues);
        
        // print!!!!
        System.out.println("New Aggregator JSON: ");
        printAsJson();
        
    }

    public String getAggregatedIssuesAsJson() throws Exception {
        return JsonUtil.toJson(aggregatedIssues);
    }
    
    private static void printAsJson() {
        try {
            String json = objectMapper.writeValueAsString(aggregatedIssues);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert aggregated issues to JSON: " + e.getMessage());
        }
    }
    
}