package com.mq.listener.MQlistener.utils;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mq.listener.MQlistener.metrics.ApplicationMetrics;
import com.mq.listener.MQlistener.models.Issue.Issue;


import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

// TODO: work out error handling for this 'Exception'
@Service
public class IssueSender {
    private static final Logger log = LoggerFactory.getLogger(IssueSender.class);

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    
    // TODO: work out definitively how to send and receive information
    private static final String POST_URL = "https://127.0.0.1:5000/issues";
    private final WebClient webClient;
    @Autowired
    public IssueSender(WebClient.Builder webClientBuilder) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        TcpClient tcpClient = TcpClient.create().secure(t -> t.sslContext(sslContext));
        this.webClient = webClientBuilder
            .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
            .baseUrl(POST_URL)
            .build();
    }

	public void sendIssue(Issue issue) throws Exception {
		System.out.println("Sending Issue:");
		issue.printIssueDetails();
		List<Issue> issueList = new ArrayList<>();
        issueList.add(issue);
        
        log.info("Sending Issue: ");
        printAsJson(issueList);
        // then send to server
        String serverResponse = postIssues(issueList);
        log.info("Response from server: " + serverResponse);
    }
	
	
	
	// sends new issue to frontend
    private String postIssues(List<Issue> issueList) {
        try {
            return webClient.post()
                    .uri(POST_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(issueList))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
        	log.info("Failed to send issues: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
	
	// prints the Issue as it is ina list
    private static void printAsJson(List<Issue> issueList) {
        try {
            String json = objectMapper.writeValueAsString(issueList);
            log.info(json);
        } catch (JsonProcessingException e) {
        	log.info("Failed to convert aggregated issues to JSON: " + e.getMessage());
        }
    }
	
}
