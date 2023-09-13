package com.mq.listener.MQlistener.config;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mq.listener.MQlistener.config.Config.QMConfig;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
public class ConfigManager {

    private Config config;
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;

    @PostConstruct
    public void init() {
        ObjectMapper mapper = new ObjectMapper();
        
        String projectRootPath = new File("").getAbsolutePath();
        File file =  new File(projectRootPath, "src/main/resources/config.json");
        System.out.println("Reading config from: " + file.getAbsolutePath());
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
        } catch (JsonMappingException e) {
            System.out.println("Configuration JSON is not in the expected format" + e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
    
    // atomic saving new config when posted
    public void saveConfigToFile() {
        ObjectMapper mapper = new ObjectMapper();
        String projectRootPath = new File("").getAbsolutePath();
        
        File tempFile = new File(projectRootPath, "src/main/resources/config.temp.json");
        File targetFile = new File(projectRootPath, "src/main/resources/config.json");
        System.out.println("Writing config to temporary file: " + tempFile.getAbsolutePath());
        try {
            // Write the config to the tempfile
            mapper.writeValue(tempFile, config);
            
            if(!tempFile.renameTo(targetFile)) {
                throw new IOException("Failed to rename temp file to target file.");
            }
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to save configuration", e);
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    public Config getConfig() {
        return config;
    }
    // TODO: test the atomicity of this and file saving
    public void updateConfigurations(ConfigDataTransferObject dto) throws Exception{
        // Clone the original config
    	System.out.println("Old Config: " + config.toString());
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
            System.out.println("New Config: " + config.toString());
            // update JSON file with new data
            saveConfigToFile();
            
        } catch (Exception e) {
            // In case of any exception, the original config remains unchanged
            e.printStackTrace();
            throw e;  // or handle it according to your needs
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



