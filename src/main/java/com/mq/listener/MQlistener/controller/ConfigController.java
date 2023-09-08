package com.mq.listener.MQlistener.controller;

//import com.mq.listener.MQlistener.config.AppConfigUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.mq.listener.MQlistener.config.AppConfig;
import com.mq.listener.MQlistener.config.ConfigDataTransferObject;
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
    
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    	
    
    
}