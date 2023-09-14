package com.mq.listener.MQlistener.utils;

import javax.net.ssl.SSLException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Service
public class SendLoginStatus {
	private static final Logger logger = LoggerFactory.getLogger(SendLoginStatus.class);
	
    private final String POST_URL = "https://127.0.0.1:5000/javaloginfeedback";
    private final WebClient webClient;

    // TODO: for a development environment SSL is ignored
    @Autowired
    public SendLoginStatus(WebClient.Builder webClientBuilder) throws SSLException {
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

    public void sendStatus(Boolean success, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);
        logger.info("Json sent to Python server: " + jsonObject.toString());
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
