package com.mq.listener.MQlistener.newConfig;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import com.mq.listener.MQlistener.newConfig.Config;

@Component
public class ConfigManager {

    private Config config;

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        
        String projectRootPath = new File("").getAbsolutePath();
        File file =  new File(projectRootPath, "src/main/resources/config.json");
        System.out.println("Reading from: " + file.getAbsolutePath());
        try {
            if (file.exists()) {
                // Read using File when running in a development environment
                this.config = mapper.readValue(file, Config.class);
            } else {
                // Fallback to ClassPathResource when running from a packaged application (like a JAR or WAR)
                try (InputStream is = new ClassPathResource("config.json").getInputStream()) {
                    this.config = mapper.readValue(is, Config.class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public Config getConfig() {
        return config;
    }

}



