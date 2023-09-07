package com.mq.listener.MQlistener.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mq.listener.MQlistener.models.Issue.Issue;


// TODO: work out error handling for this 'Exception'
@Service
public class IssueSender {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    // TODO: work out definitively how to send and receive information
    private static final String ENDPOINT_URL = "https://127.0.0.1:5000/issues";
    private final WebClient webClient;
    @Autowired
    public IssueSender(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(ENDPOINT_URL).build();
    }

	public void sendIssue(Issue issue) throws Exception {
		
		List<Issue> issueList = new ArrayList<>();
        issueList.add(issue);
        
        System.out.println("Sending Issue: ");
        printAsJson(issueList);
        // then send to server
        String serverResponse = postIssues(issueList);
        System.out.println("Response from server: " + serverResponse);
    }
	
	// sends new issue to frontend
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
	
	// prints the Issue as it is ina list
    private static void printAsJson(List<Issue> issueList) {
        try {
            String json = objectMapper.writeValueAsString(issueList);
            System.out.println(json);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert aggregated issues to JSON: " + e.getMessage());
        }
    }
	
}
