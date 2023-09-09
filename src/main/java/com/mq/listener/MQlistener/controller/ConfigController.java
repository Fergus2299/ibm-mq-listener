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

            // Check and convert data, throwing an exception if something's wrong
            validateAndConvertDTO(configDTO);

            System.out.println("Apps Config:");
//            System.out.println("  ConnThreshold: " + configDTO.getRetrievedThresholds().getApps().getConnThreshold());
//            System.out.println("  ConnOpRatioThreshold: " + configDTO.getRetrievedThresholds().getApps().getConnOpRatioThreshold());
//            System.out.println("  MinimumConns: " + configDTO.getRetrievedThresholds().getApps().getMinimumConns());
            
            appConfig.getConnections().setMax(configDTO.getRetrievedThresholds().getApps().getConnThreshold());
            Map<String, Object> connectionOperationsRatio = new HashMap<>();
            connectionOperationsRatio.put("max", configDTO.getRetrievedThresholds().getApps().getConnOpRatioThreshold());
            connectionOperationsRatio.put("connections", configDTO.getRetrievedThresholds().getApps().getMinimumConns());
            appConfig.setConnectionOperationsRatio(connectionOperationsRatio);
            appConfig.print();
            

            System.out.println("Queue Manager Config:");
//            System.out.println("  ErrorThreshold: " + configDTO.getRetrievedThresholds().getQueue_manager().getErrorThreshold());
//            System.out.println("  MaxMQConns: " + configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQConns());
//            System.out.println("  MaxMQOps: " + configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQOps());

            queueManagerConfig.getErrors().setMax(configDTO.getRetrievedThresholds().getQueue_manager().getErrorThreshold());
            queueManagerConfig.getConnections().setMax(configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQConns());
            queueManagerConfig.getOperations().setMax(configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQOps());
            
            queueManagerConfig.print();
            
            System.out.println("Queues Config:");
//            System.out.println("  ErrorThreshold: " + configDTO.getRetrievedThresholds().getQueues().getErrorThreshold());
            
            Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholds = configDTO.getRetrievedThresholds().getQueues().getQueueThresholds();
            for (String queueName : queueThresholds.keySet()) {
                ConfigDataTransferObject.QueueThresholdDTO queueThresholdDTO = queueThresholds.get(queueName);
                System.out.println("  " + queueName + " - Depth: " + queueThresholdDTO.getDepth() + ", Activity: " + queueThresholdDTO.getActivity());
            }

            queueConfig.getErrors().setMax(configDTO.getRetrievedThresholds().getQueues().getErrorThreshold());
            Map<String, Integer> queueThresholdsMap = new HashMap<>();
            for (Map.Entry<String, ConfigDataTransferObject.QueueThresholdDTO> entry : configDTO.getRetrievedThresholds().getQueues().getQueueThresholds().entrySet()) {
                queueThresholdsMap.put(entry.getKey(), entry.getValue().getActivity());
            }
            queueConfig.setOperationsSpecificQueues(queueThresholdsMap);
            queueConfig.print();
            return new ResponseEntity<>("Configuration updated successfully!", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error updating configuration: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    
    private void validateAndConvertDTO(ConfigDataTransferObject configDTO) throws Exception {
        // Apps Config
        Object connThreshold = configDTO.getRetrievedThresholds().getApps().getConnThreshold();
        if (!(connThreshold instanceof Integer)) {
            throw new Exception("Invalid ConnThreshold value for Apps. Expected an integer.");
        }

        Object connOpRatioThreshold = configDTO.getRetrievedThresholds().getApps().getConnOpRatioThreshold();
        if (!(connOpRatioThreshold instanceof Float) && !(connOpRatioThreshold instanceof Double)) {
            throw new Exception("Invalid ConnOpRatioThreshold value for Apps. Expected a float.");
        }

        Object minimumConns = configDTO.getRetrievedThresholds().getApps().getMinimumConns();
        if (!(minimumConns instanceof Integer)) {
            throw new Exception("Invalid MinimumConns value for Apps. Expected an integer.");
        }

        // Queue Manager Config
        Object errorThresholdQM = configDTO.getRetrievedThresholds().getQueue_manager().getErrorThreshold();
        if (!(errorThresholdQM instanceof Integer)) {
            throw new Exception("Invalid ErrorThreshold value for Queue Manager. Expected an integer.");
        }

        Object maxMQConns = configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQConns();
        if (!(maxMQConns instanceof Integer)) {
            throw new Exception("Invalid MaxMQConns value for Queue Manager. Expected an integer.");
        }

        Object maxMQOps = configDTO.getRetrievedThresholds().getQueue_manager().getMaxMQOps();
        if (!(maxMQOps instanceof Integer)) {
            throw new Exception("Invalid MaxMQOps value for Queue Manager. Expected an integer.");
        }

        // Queues Config
        Object errorThresholdQ = configDTO.getRetrievedThresholds().getQueues().getErrorThreshold();
        if (!(errorThresholdQ instanceof Integer)) {
            throw new Exception("Invalid ErrorThreshold value for Queues. Expected an integer.");
        }

        // Here, I'm assuming that the QueueThresholds values also need to be checked
        Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholds = configDTO.getRetrievedThresholds().getQueues().getQueueThresholds();
        for (ConfigDataTransferObject.QueueThresholdDTO queueThresholdDTO : queueThresholds.values()) {
            Object depth = queueThresholdDTO.getDepth();
            if (!(depth instanceof Integer)) {
                throw new Exception("Invalid Depth value for Queue. Expected an integer.");
            }

            Object activity = queueThresholdDTO.getActivity();
            if (!(activity instanceof Integer)) {
                throw new Exception("Invalid Activity value for Queue. Expected an integer.");
            }
        }
    }
    
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    	
    
    
}