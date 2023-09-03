//package com.mq.listener.MQlistener.utils;
//
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import com.mq.listener.MQlistener.models.Issue.Issue;
//
//@Service
//public class IssueService {
//    private final RestTemplate restTemplate;
//
//    public IssueService(RestTemplate restTemplate) {
//        this.restTemplate = restTemplate;
//    }
//
//    public void postIssue(Issue issue) {
//        String url = "https://127.0.0.1:5000/issues";
//        
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        HttpEntity<Issue> request = new HttpEntity<>(issue, headers);
//
//        restTemplate.postForObject(url, request, Void.class);
//    }
//}