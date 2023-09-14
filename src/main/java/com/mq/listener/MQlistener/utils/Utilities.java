package com.mq.listener.MQlistener.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Utilities {
	private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
	
    @Value("${ibm.mq.connName}")
    private String connName;
    
    private String address;
    private Integer port;
	
    // extracting address and port from conn name
    public void extractConnName() throws Exception {
    	Pattern pattern = Pattern.compile("^(.+)\\((\\d+)\\)$");
    	Matcher matcher = pattern.matcher(connName);
        if (matcher.find()) {
            address = matcher.group(1);
            port = Integer.parseInt(matcher.group(2));
            System.out.println("Address: " + address);
            System.out.println("Port: " + port);
        } else {
        	throw new Exception("Incorrect IP Address or Application Config format.");
        }
    }
    
    public String formatNow() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(timeFormatter);
    }
    // returns year also
    public String prettyDateTime() {
        LocalDateTime now = LocalDateTime.now();
        return now.format(timeFormatter);
    }
}