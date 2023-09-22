package com.mq.listener.MQlistener.config;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mq.listener.MQlistener.config.Config.QMConfig;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    protected static final String BASE_PATH = "config/";

    private Config config;
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;

    // default value is used unless app.config.path is set
    @Value("${app.config.path:src/main/resources/}")
    private String configPath;


    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(BASE_PATH + "config.json");
        logger.info("Reading config from: {}", file.getAbsolutePath());
        try {
            if (file.exists()) {
                System.out.println("file exists");
                // Read using File when running in a development environment
                this.config = mapper.readValue(file, Config.class);
            } else {
                // Fallback to ClassPathResource when running from a packaged application (like a JAR or WAR)
                try (InputStream is = new ClassPathResource("config.json").getInputStream()) {
                    this.config = mapper.readValue(is, Config.class);
                }
            }
        } catch (JsonMappingException e) {
        	logger.error("Configuration JSON is not in the expected format" + e);
        } catch (IOException e) {
        	logger.error("Failed to load configuration", e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    // atomic saving new config when posted
    public void saveConfigToFile() {
        ObjectMapper mapper = new ObjectMapper();
        Path tempFilePath = Path.of(BASE_PATH, "configTemp.json");
        Path targetFilePath = Path.of(BASE_PATH, "config.json");
        logger.info("Writing config to temporary file: {}", tempFilePath.toString());
        try {
            // Write the config to the tempfile
            mapper.writeValue(tempFilePath.toFile(), config);
            Files.deleteIfExists(targetFilePath);
            Files.move(tempFilePath, targetFilePath);
            logger.info("New config successfully saved");
        } catch (IOException e) {
            logger.error("Failed to save configuration" + e);
            throw new RuntimeException("Failed to save configuration", e);
        }
    }

    public Config getConfig() {
        return config;
    }
    // TODO: test the atomicity of this and file saving
    public void updateConfigurations(ConfigDataTransferObject dto) throws Exception {
        // Clone the original config
    	logger.info("Old Config: " + config.toString());
        Config clonedConfig = deepClone(config);

        try {
            // Update the cloned config
            QMConfig queueManagerConfig = 
                 clonedConfig
                 .getQms()
                 .getOrDefault(qMgrName, clonedConfig.getQms().get("<DEFAULT>"));
            queueManagerConfig.getApp().updateFromDTO(dto.getRetrievedThresholds().getApps());
            queueManagerConfig.getQueueManager().updateFromDTO(dto.getRetrievedThresholds().getQueue_manager());
            queueManagerConfig.getQueue().updateFromDTO(dto.getRetrievedThresholds().getQueues());
            clonedConfig.getQms().put(qMgrName, queueManagerConfig);

            // if all is good, the original config is replaced
            config = clonedConfig;
            logger.info("New Config: " + config.toString());
            // update JSON file with new data
            saveConfigToFile();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    // clones Config object
    private Config deepClone(Config original) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(mapper.writeValueAsString(original), Config.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to clone configuration", e);
        }
    }
}



