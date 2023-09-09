package com.mq.listener.MQlistener.controller;

import java.util.HashMap;
import java.util.Map;

//import com.mq.listener.MQlistener.config.AppConfigUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.mq.listener.MQlistener.config.AppConfig;
import com.mq.listener.MQlistener.config.ConfigDataTransferObject;
import com.mq.listener.MQlistener.config.ConfigUpdateRequest;
import com.mq.listener.MQlistener.config.QueueConfig;
import com.mq.listener.MQlistener.config.QueueManagerConfig;

@RestController
public class ConfigController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private QueueConfig queueConfig;

    @Autowired
    private QueueManagerConfig queueManagerConfig;
    @GetMapping("/configurations")
    public ConfigDataTransferObject getConfigurations() {
        System.out.println("Config requested by frontend");
        
        ConfigDataTransferObject dataTransferObject = new ConfigDataTransferObject();
        ConfigDataTransferObject.RetrievedThresholdsDTO retrievedThresholdsDTO = new ConfigDataTransferObject.RetrievedThresholdsDTO();
        
        ConfigDataTransferObject.AppDTO appDTO = new ConfigDataTransferObject.AppDTO();
        appDTO.setConnThreshold(appConfig.getConnections().getMax());
        Number ratioNum = (Number) appConfig.getConnectionOperationsRatio().get("max");
        appDTO.setConnOpRatioThreshold(ratioNum.floatValue());
        appDTO.setMinimumConns((Integer) appConfig.getConnectionOperationsRatio().get("connections"));

        ConfigDataTransferObject.QueueManagerDTO queueManagerDTO = new ConfigDataTransferObject.QueueManagerDTO();
        queueManagerDTO.setErrorThreshold(queueManagerConfig.getErrors().getMax());
        queueManagerDTO.setMaxMQConns(queueManagerConfig.getConnections().getMax());
        queueManagerDTO.setMaxMQOps(queueManagerConfig.getOperations().getMax());

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
    
    
    @PostMapping("/updateConfig")
    public ResponseEntity<String> updateConfig(@RequestBody ConfigDataTransferObject configDTO) {
        try {
            System.out.println("Received Configuration:");

            System.out.println("Apps Config:");
            System.out.println("  ConnThreshold: " + configDTO.getRetrievedThresholds().getApps().getConnThreshold());
            System.out.println("  ConnOpRatioThreshold: " + configDTO.getRetrievedThresholds().getApps().getConnOpRatioThreshold());
            System.out.println("  MinimumConns: " + configDTO.getRetrievedThresholds().getApps().getMinimumConns());

            System.out.println("Queue Manager Config:");
            System.out.println("  ErrorThreshold: " + configDTO.getRetrievedThresholds().getQueue_manager().getErrorThreshold());
            System.out.println("  MaxMQConns: " + configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQConns());
            System.out.println("  MaxMQOps: " + configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQOps());

            System.out.println("Queues Config:");
            System.out.println("  ErrorThreshold: " + configDTO.getRetrievedThresholds().getQueues().getErrorThreshold());

            Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholds = configDTO.getRetrievedThresholds().getQueues().getQueueThresholds();
            for (String queueName : queueThresholds.keySet()) {
                ConfigDataTransferObject.QueueThresholdDTO queueThresholdDTO = queueThresholds.get(queueName);
                System.out.println("  " + queueName + " - Depth: " + queueThresholdDTO.getDepth() + ", Activity: " + queueThresholdDTO.getActivity());
            }

            // Logic to actually update your configuration based on the received DTO goes here...

            return new ResponseEntity<>("Configuration updated successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating configuration: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    	
    
    
}