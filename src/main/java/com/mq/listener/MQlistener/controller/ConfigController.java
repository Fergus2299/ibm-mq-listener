package com.mq.listener.MQlistener.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.mq.listener.MQlistener.config.AppConfigUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mq.listener.MQlistener.config.ConfigDTO;
import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.config.Config.QMConfig;
import com.mq.listener.MQlistener.config.Config.QMConfig.AppConfig;
import com.mq.listener.MQlistener.config.Config.QMConfig.QueueConfig;
import com.mq.listener.MQlistener.config.Config.QMConfig.QueueManagerConfig;
import com.mq.listener.MQlistener.processors.AccountingProcessor;

import org.springframework.http.HttpStatus;

@RestController
public class ConfigController {
	private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
	@Autowired
	private Environment env;

    @Autowired
    private ConfigManager configManager;
    
	// injecting qMgrName property
	@Value("${ibm.mq.queueManager}")
	private String qMgrName;

    @GetMapping("/configurations")
    public ConfigDTO getConfigurations() {
        log.info("Config requested by frontend");
        System.out.println(configManager
            	.getConfig());
        System.out.println(qMgrName);
//    	QMConfig queueManagerConfig = 
//    	configManager
//    	.getConfig()
//    	.getQms()
//    	.getOrDefault(
//    			qMgrName, 
//    			configManager.getConfig().getQms().get("<DEFAULT>"));
    	
    	// loading all subcategories of config
//    	AppConfig appConfig = queueManagerConfig.getApp();
//    	QueueManagerConfig QMConfig = queueManagerConfig.getQueueManager();
//    	QueueConfig queueConfig = queueManagerConfig.getQueue();
//    	
//        
//        ConfigDataTransferObject dataTransferObject = new ConfigDataTransferObject();
//        ConfigDataTransferObject.RetrievedThresholdsDTO retrievedThresholdsDTO = new ConfigDataTransferObject.RetrievedThresholdsDTO();
//        
//        ConfigDataTransferObject.AppDTO appDTO = new ConfigDataTransferObject.AppDTO();
//        appDTO.setConnThreshold(appConfig.getConnections().getMax());
//        Number ratioNum = (Number) appConfig.getConnectionOperationsRatio().getMax();
//        appDTO.setConnOpRatioThreshold(ratioNum.floatValue());
//        appDTO.setMinimumConns((Integer) appConfig.getConnectionOperationsRatio().getConnections());
//
//        ConfigDataTransferObject.QueueManagerDTO queueManagerDTO = new ConfigDataTransferObject.QueueManagerDTO();
//        queueManagerDTO.setErrorThreshold(QMConfig.getErrors().getMax());
//        queueManagerDTO.setMaxMQConns(QMConfig.getConnections().getMax());
//        queueManagerDTO.setMaxMQOps(QMConfig.getOperations().getMax());
//
//        ConfigDataTransferObject.QueueDTO queueDTO = new ConfigDataTransferObject.QueueDTO();
//        queueDTO.setErrorThreshold(queueConfig.getErrors().getMax());
//        Map<String, Integer> queueActivityMap = queueConfig.getOperationsSpecificQueues();
//        Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholdsMap = new HashMap<>();
//        for (String key : queueActivityMap.keySet()) {
//            ConfigDataTransferObject.QueueThresholdDTO queueThresholdDTO = new ConfigDataTransferObject.QueueThresholdDTO();
//            queueThresholdDTO.setActivity(queueActivityMap.get(key));
//            queueThresholdsMap.put(key, queueThresholdDTO);
//        }
//        queueDTO.setQueueThresholds(queueThresholdsMap);
//
//        // Put all DTOs in RetrievedThresholdsDTO
//        retrievedThresholdsDTO.setApps(appDTO);
//        retrievedThresholdsDTO.setQueue_manager(queueManagerDTO);
//        retrievedThresholdsDTO.setQueues(queueDTO);
    	ConfigDTO dataTransferObject = configManager.getConfig().toConfigDataTransferObject(qMgrName);

        log.info("Sent following config to frontend: " + configManager
            	.getConfig().toString());
        return dataTransferObject;
    }
    
    @PostMapping("/updateConfig")
    public ResponseEntity<String> updateConfig(@RequestBody ConfigDTO configDTO) {
        try {
            log.info("Checking posted configuration...");
            validateAndConvertDTO(configDTO);
            try {
            	System.out.println("Recieved new DTO: " + configDTO.toString());
                configManager.updateConfigurations(configDTO);
                return new ResponseEntity<>("Configuration updated successfully!", HttpStatus.OK);
            } catch (Exception e) {
            	log.error(e.getMessage());
                return new ResponseEntity<>("An error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            
        } catch (Exception e) {
        	log.error(e.getMessage());
            return new ResponseEntity<>("Error updating configuration: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    private void validateAndConvertDTO(ConfigDTO configDTO) throws Exception {
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
        Map<String, ConfigDTO.QueueThresholdDTO> queueThresholds = configDTO.getRetrievedThresholds().getQueues().getQueueThresholds();
        for (ConfigDTO.QueueThresholdDTO queueThresholdDTO : queueThresholds.values()) {


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