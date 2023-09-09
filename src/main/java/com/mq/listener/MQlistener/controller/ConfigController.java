package com.mq.listener.MQlistener.controller;

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
    	ConfigDataTransferObject.AppDTO appDTO = new ConfigDataTransferObject.AppDTO();
        appDTO.setConnThreshold(appConfig.getConnections().getMax());
        // handling double and float
        Number ratioNum = (Number) appConfig.getConnectionOperationsRatio().get("max");
        appDTO.setConnOpRatioThreshold(ratioNum.floatValue());
        appDTO.setMinimumConns((Integer) appConfig.getConnectionOperationsRatio().get("connections"));
        
        ConfigDataTransferObject.QueueManagerDTO queueManagerDTO = new ConfigDataTransferObject.QueueManagerDTO();
        queueManagerDTO.setErrorThreshold(queueManagerConfig.getErrors().getMax());
        queueManagerDTO.setMaxMQConns(queueManagerConfig.getConnections().getMax());
        queueManagerDTO.setMaxMQOps(queueManagerConfig.getOperations().getMax());

        ConfigDataTransferObject.QueueDTO queueDTO = new ConfigDataTransferObject.QueueDTO();
        queueDTO.setErrorThreshold(queueConfig.getErrors().getMax());
        queueDTO.setQueueActivityThresholds(queueConfig.getOperationsSpecificQueues());
        
        // putting them all in DTO
        dataTransferObject.setApps(appDTO);
        dataTransferObject.setQueue_manager(queueManagerDTO);
        dataTransferObject.setQueues(queueDTO);
        
        return dataTransferObject;
    }
    
    
    @PostMapping("/updateAppConfig")
    public String updateAppConfig(@RequestBody ConfigDataTransferObject configDTO) {
        try {
            System.out.println("Received Configuration:");
            System.out.println("Apps Config:");
            System.out.println("  ConnThreshold: " + configDTO.getApps().getConnThreshold());
            System.out.println("  ConnOpRatioThreshold: " + configDTO.getApps().getConnOpRatioThreshold());
            System.out.println("  MinimumConns: " + configDTO.getApps().getMinimumConns());
            
            System.out.println("Queue Manager Config:");
            System.out.println("  ErrorThreshold: " + configDTO.getQueue_manager().getErrorThreshold());
            System.out.println("  MaxMQConns: " + configDTO.getQueue_manager().getMaxMQConns());
            System.out.println("  MaxMQOps: " + configDTO.getQueue_manager().getMaxMQOps());
            
            System.out.println("Queues Config:");
            System.out.println("  ErrorThreshold: " + configDTO.getQueues().getErrorThreshold());
            System.out.println("  QueueActivityThresholds: " + configDTO.getQueues().getQueueActivityThresholds());
            
            return "App configuration updated successfully!";
        } catch (Exception e) {
            return "Error updating app configuration: " + e.getMessage();
        }
    }
    
    
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    	
    
    
}