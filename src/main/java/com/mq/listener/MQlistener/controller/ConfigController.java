package com.mq.listener.MQlistener.controller;

import java.util.HashMap;
import java.util.Map;

//import com.mq.listener.MQlistener.config.AppConfigUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mq.listener.MQlistener.config.ConfigDataTransferObject;
import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.config.Config.QMConfig;
import com.mq.listener.MQlistener.config.Config.QMConfig.AppConfig;
import com.mq.listener.MQlistener.config.Config.QMConfig.QueueConfig;
import com.mq.listener.MQlistener.config.Config.QMConfig.QueueManagerConfig;

import org.springframework.http.HttpStatus;

@RestController
public class ConfigController {


    @Autowired
    private ConfigManager configManager;
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;
	

    
    
    @GetMapping("/configurations")
    public ConfigDataTransferObject getConfigurations() {
        System.out.println("Config requested by frontend");
        
    	QMConfig queueManagerConfig = 
    	configManager
    	.getConfig()
    	.getQms()
    	.getOrDefault(
    			qMgrName, 
    			configManager.getConfig().getQms().get("<DEFAULT>"));
    	
    	// loading all subcategories of config
    	AppConfig appConfig = queueManagerConfig.getApp();
    	QueueManagerConfig QMConfig = queueManagerConfig.getQueueManager();
    	QueueConfig queueConfig = queueManagerConfig.getQueue();
    	
        
        ConfigDataTransferObject dataTransferObject = new ConfigDataTransferObject();
        ConfigDataTransferObject.RetrievedThresholdsDTO retrievedThresholdsDTO = new ConfigDataTransferObject.RetrievedThresholdsDTO();
        
        ConfigDataTransferObject.AppDTO appDTO = new ConfigDataTransferObject.AppDTO();
        appDTO.setConnThreshold(appConfig.getConnections().getMax());
        Number ratioNum = (Number) appConfig.getConnectionOperationsRatio().getMax();
        appDTO.setConnOpRatioThreshold(ratioNum.floatValue());
        appDTO.setMinimumConns((Integer) appConfig.getConnectionOperationsRatio().getConnections());

        ConfigDataTransferObject.QueueManagerDTO queueManagerDTO = new ConfigDataTransferObject.QueueManagerDTO();
        queueManagerDTO.setErrorThreshold(QMConfig.getErrors().getMax());
        queueManagerDTO.setMaxMQConns(QMConfig.getConnections().getMax());
        queueManagerDTO.setMaxMQOps(QMConfig.getOperations().getMax());

        ConfigDataTransferObject.QueueDTO queueDTO = new ConfigDataTransferObject.QueueDTO();
        queueDTO.setErrorThreshold(queueConfig.getErrors().getMax());
        Map<String, Integer> queueActivityMap = queueConfig.getOperationsSpecificQueues();
        Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholdsMap = new HashMap<>();
        for (String key : queueActivityMap.keySet()) {
            ConfigDataTransferObject.QueueThresholdDTO queueThresholdDTO = new ConfigDataTransferObject.QueueThresholdDTO();
            queueThresholdDTO.setActivity(queueActivityMap.get(key));
            queueThresholdsMap.put(key, queueThresholdDTO);
        }
        queueDTO.setQueueThresholds(queueThresholdsMap);

        // Put all DTOs in RetrievedThresholdsDTO
        retrievedThresholdsDTO.setApps(appDTO);
        retrievedThresholdsDTO.setQueue_manager(queueManagerDTO);
        retrievedThresholdsDTO.setQueues(queueDTO);
        
        // Put RetrievedThresholdsDTO in main DTO
        dataTransferObject.setRetrievedThresholds(retrievedThresholdsDTO);

        return dataTransferObject;
    }
    
    // TODO: ensure that update is atomic - ie everything is rolled back if something fails
    @PostMapping("/updateConfig")
    public ResponseEntity<String> updateConfig(@RequestBody ConfigDataTransferObject configDTO) {
        try {
            System.out.println("Checking posted configuration...");
            validateAndConvertDTO(configDTO);
            try {
                configManager.updateConfigurations(configDTO);
                return new ResponseEntity<>("Configuration updated successfully!", HttpStatus.OK);
            } catch (Exception e) {
                // Log exception
                return new ResponseEntity<>("An error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
        	System.out.println(e.getMessage());
            return new ResponseEntity<>("Error updating configuration: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    private void validateAndConvertDTO(ConfigDataTransferObject configDTO) throws Exception {
        // Apps Config
        Integer connThreshold = configDTO.getRetrievedThresholds().getApps().getConnThreshold();
        if (connThreshold == null || connThreshold <= 0) {
            throw new Exception("Invalid or missing ConnThreshold value for Apps. Expected a positive integer.");
        }

        Float connOpRatioThreshold = configDTO.getRetrievedThresholds().getApps().getConnOpRatioThreshold();
        if (connOpRatioThreshold == null || connOpRatioThreshold <= 0) {
            throw new Exception("Invalid or missing ConnOpRatioThreshold value for Apps. Expected a positive float.");
        }

        Integer minimumConns = configDTO.getRetrievedThresholds().getApps().getMinimumConns();
        if (minimumConns == null || minimumConns <= 0) {
            throw new Exception("Invalid or missing MinimumConns value for Apps. Expected a positive integer.");
        }

        // Queue Manager Config
        Integer errorThresholdQM = configDTO.getRetrievedThresholds().getQueue_manager().getErrorThreshold();
        if (errorThresholdQM == null || errorThresholdQM <= 0) {
            throw new Exception("Invalid or missing ErrorThreshold value for Queue Manager. Expected a positive integer.");
        }

        Integer maxMQConns = configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQConns();
        if (maxMQConns == null || maxMQConns <= 0) {
            throw new Exception("Invalid or missing MaxMQConns value for Queue Manager. Expected a positive integer.");
        }

        Integer maxMQOps = configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQOps();
        if (maxMQOps == null || maxMQOps <= 0) {
            throw new Exception("Invalid or missing MaxMQOps value for Queue Manager. Expected a positive integer.");
        }

        // Queues Config
        Integer errorThresholdQ = configDTO.getRetrievedThresholds().getQueues().getErrorThreshold();
        if (errorThresholdQ == null || errorThresholdQ <= 0) {
            throw new Exception("Invalid or missing ErrorThreshold value for Queues. Expected a positive integer.");
        }

        // Checking QueueThresholds values
        Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholds = configDTO.getRetrievedThresholds().getQueues().getQueueThresholds();
        for (ConfigDataTransferObject.QueueThresholdDTO queueThresholdDTO : queueThresholds.values()) {


            Integer activity = queueThresholdDTO.getActivity();
            if (activity == null || activity <= 0) {
                throw new Exception("Invalid or missing Activity value for Queue. Expected a positive integer.");
            }
        }
    }
    
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    	
    
    
}