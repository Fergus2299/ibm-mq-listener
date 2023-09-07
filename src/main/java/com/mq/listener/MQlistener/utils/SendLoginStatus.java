package com.mq.listener.MQlistener.utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.core.publisher.Mono;

@Service
public class SendLoginStatus {
    private final String POST_URL = "https://127.0.0.1:5000/javaloginfeedback";
    private final WebClient webClient;

    @Autowired
    public SendLoginStatus(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(POST_URL).build();
    }

    public void sendStatus(Boolean success, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);

        webClient.post()
                .uri(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(jsonObject.toString()), String.class)
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(
                        response -> System.out.println("POST request successful. Response: " + response),
                        error -> {
                            if (error instanceof WebClientResponseException) {
                                WebClientResponseException webClientError = (WebClientResponseException) error;
                                System.err.println("Error " + webClientError.getStatusCode() + ": " + webClientError.getResponseBodyAsString());
                            } else {
                                System.err.println("Error sending POST request: " + error.getMessage());
                            }
                        }
                );
    }
}
