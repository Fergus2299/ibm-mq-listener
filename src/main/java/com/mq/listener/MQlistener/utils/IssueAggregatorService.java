package com.mq.listener.MQlistener.utils;

import java.net.http.HttpHeaders;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mq.listener.MQlistener.models.Issue.Issue;

@Service
public class IssueAggregatorService {
    private static final Map<String, Object> aggregatedIssues = new HashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    private static final String ENDPOINT_URL = "https://127.0.0.1:5000/issues";
    private final WebClient webClient;
    @Autowired
    public IssueAggregatorService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(ENDPOINT_URL).build();
    }
    
    public void sendIssues(String type, Object issues) throws Exception {
        // Update the aggregated issues map based on type
        aggregatedIssues.put(type, issues);
        List<Issue> issueList = convertToIssueList(aggregatedIssues);
        
        // print!!!!
        System.out.println("New Aggregator JSON: ");
        printAsJson(issueList);
        String serverResponse = postIssues(issueList);
        System.out.println("Response from server: " + serverResponse);
    }

    public String getAggregatedIssuesAsJson() throws Exception {
        return JsonUtil.toJson(aggregatedIssues);
    }
    
    private static void printAsJson(List<Issue> issueList) {
        try {
            String json = objectMapper.writeValueAsString(issueList);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert aggregated issues to JSON: " + e.getMessage());
        }
    }
    
    private String postIssues(List<Issue> issueList) {
        try {
            return webClient.post()
                    .uri(ENDPOINT_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(issueList))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            System.out.println("Failed to send issues: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private static List<Issue> convertToIssueList(Object issues) {
	    try {
	        Map<String, Map<String, Issue>> issueMap = (Map<String, Map<String, Issue>>) issues;
	        return convertMapToList(issueMap);
	    } catch (ClassCastException e) {
	        throw new IllegalArgumentException("Type conversion error: " + e.getMessage(), e);
	    }
    }

    private static List<Issue> convertMapToList(Map<String, Map<String, Issue>> issueMap) {
        List<Issue> issueList = new ArrayList<>();
        for (Map<String, Issue> nestedMap : issueMap.values()) {
            issueList.addAll(nestedMap.values());
        }
        return issueList;
    }
}