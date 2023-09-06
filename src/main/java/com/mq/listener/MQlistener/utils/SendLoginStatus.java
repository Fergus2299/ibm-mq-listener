package com.mq.listener.MQlistener.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SendLoginStatus {
	// TODO: change to web client
	private final String POST_URL = "https://your-api-endpoint.com/login-status";
	
    public void sendStatus(Boolean success, String message) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(message);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(POST_URL, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("POST request successful. Response: " + response.getBody());
            } else {
                System.out.println("POST request returned an unexpected status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            // Specific exception for 4xx errors
            System.err.println("Error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            // Base exception for RestTemplate, catches general network issues
            System.err.println("Error sending POST request: " + e.getMessage());
        } catch (Exception e) {
            // Handle other exceptions that may occur
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }
        
}
