package com.mq.listener.MQlistener.utils;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mq.listener.MQlistener.models.Issue;

import java.io.File;
import java.io.IOException;

public class LoggingService {

    private static final String LOG_DIRECTORY = "logs/"; // Define your logging directory
    private static final String FILE_EXTENSION = ".json";
    private ObjectMapper objectMapper;

    public LoggingService() {
        this.objectMapper = new ObjectMapper();
        File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }

    public void logIssue(Issue issue) {
        String fileName = LOG_DIRECTORY + issue.getIssueCode() + "_" + issue.getStartTimeStamp().replace(":", "-") + FILE_EXTENSION; // We replace colons for file-naming compatibility

        try {
            objectMapper.writeValue(new File(fileName), issue);
            System.out.println("Logged issue to " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to log issue: " + e.getMessage());
        }
    }
}