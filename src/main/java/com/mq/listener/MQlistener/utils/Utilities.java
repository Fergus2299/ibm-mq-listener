package com.mq.listener.MQlistener.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;

public class Utilities {
	
    @Value("${ibm.mq.connName}")
    private String connName;
    
    private String address;
    private Integer port;
	
    // extracting address and port from conn name
    public void extractConnName() {
    	Pattern pattern = Pattern.compile("^(.+)\\((\\d+)\\)$");
    	Matcher matcher = pattern.matcher(connName);
        if (matcher.find()) {
            address = matcher.group(1);
            port = Integer.parseInt(matcher.group(2));
            System.out.println("Address: " + address);
            System.out.println("Port: " + port);
        } else {
        	
        }
    }
}
