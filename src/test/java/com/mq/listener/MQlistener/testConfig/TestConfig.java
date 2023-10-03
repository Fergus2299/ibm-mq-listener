package com.mq.listener.MQlistener.testConfig;

import java.util.HashMap;
import java.util.Map;

import com.mq.listener.MQlistener.config.Config;
import com.mq.listener.MQlistener.config.ConfigDataTransferObject;

public class TestConfig {
	
    public static Config createSampleConfig1() {
        Config config = new Config();
        Config.QMConfig qmConfig = new Config.QMConfig();

        // App Configurations
        Config.QMConfig.AppConfig appConfig = new Config.QMConfig.AppConfig();
        Config.QMConfig.AppConfig.ConnectionConfig appConnectionConfig = new Config.QMConfig.AppConfig.ConnectionConfig();
        appConnectionConfig.setMax(100); // set max connections for app
        appConfig.setConnections(appConnectionConfig);

        Config.QMConfig.AppConfig.ConnectionOperationsRatioConfig connectionOperationsRatioConfig = new Config.QMConfig.AppConfig.ConnectionOperationsRatioConfig();
        connectionOperationsRatioConfig.setMax(0.8); // set ratio max for demonstration
        connectionOperationsRatioConfig.setConnections(10); // set connection number for ratio
        appConfig.setConnectionOperationsRatio(connectionOperationsRatioConfig);
        
        // Queue Manager Configurations
        Config.QMConfig.QueueManagerConfig queueManagerConfig = new Config.QMConfig.QueueManagerConfig();
        Config.QMConfig.QueueManagerConfig.ConnectionConfig qmConnectionConfig = new Config.QMConfig.QueueManagerConfig.ConnectionConfig();
        qmConnectionConfig.setMax(200); // set max connections for QM
        queueManagerConfig.setConnections(qmConnectionConfig);
        
        Config.QMConfig.QueueManagerConfig.OperationsConfig operationsConfig = new Config.QMConfig.QueueManagerConfig.OperationsConfig();
        operationsConfig.setMax(500); // set max operations for QM
        queueManagerConfig.setOperations(operationsConfig);
        
        Config.QMConfig.QueueManagerConfig.ErrorsConfig errorsConfig = new Config.QMConfig.QueueManagerConfig.ErrorsConfig();
        errorsConfig.setMax(10); // set max errors for QM
        queueManagerConfig.setErrors(errorsConfig);
        
        // Queue Configurations
        Config.QMConfig.QueueConfig queueConfig = new Config.QMConfig.QueueConfig();
        Config.QMConfig.QueueConfig.ErrorsConfig queueErrorsConfig = new Config.QMConfig.QueueConfig.ErrorsConfig();
        queueErrorsConfig.setMax(5); // set max errors for queue
        queueConfig.setErrors(queueErrorsConfig);
        queueConfig.setOperationsDefault(Config.OPERATIONS_DEFAULT);
        queueConfig.setOperationsSpecificQueues(Map.of("Queue1", 200, "Queue2", 400)); // set specific operations for queues
        
        // Setting the configurations to qmConfig
        qmConfig.setApp(appConfig);
        qmConfig.setQueueManager(queueManagerConfig);
        qmConfig.setQueue(queueConfig);
        
        config.setQms(Map.of("QM1", qmConfig));
        return config;
    }
    public static Config createSampleConfig2() {
    	Config config = createSampleConfig1();
        
        Config.QMConfig qmConfig = config.getQms().get("QM1");
        Config.QMConfig.AppConfig appConfig = qmConfig.getApp();
        Config.QMConfig.AppConfig.ConnectionConfig appConnectionConfig = appConfig.getConnections();

        appConnectionConfig.setMax(200);

        Config.QMConfig.AppConfig.ConnectionOperationsRatioConfig connectionOperationsRatioConfig = appConfig.getConnectionOperationsRatio();
        connectionOperationsRatioConfig.setMax(0.95);
        connectionOperationsRatioConfig.setConnections(44);

        return config;
    }
    
    
    
    public static ConfigDataTransferObject createSampleDto1() {
	    ConfigDataTransferObject dto = new ConfigDataTransferObject();
	
	    ConfigDataTransferObject.RetrievedThresholdsDTO retrievedThresholdsDTO = new ConfigDataTransferObject.RetrievedThresholdsDTO();
	    
	    ConfigDataTransferObject.AppDTO appDTO = new ConfigDataTransferObject.AppDTO();
	    appDTO.setConnThreshold(10);
	    appDTO.setConnOpRatioThreshold(0.5f);
	    appDTO.setMinimumConns(4);
	    
	    ConfigDataTransferObject.QueueManagerDTO queueManagerDTO = new ConfigDataTransferObject.QueueManagerDTO();
	    queueManagerDTO.setErrorThreshold(2);
	    queueManagerDTO.setMaxMQConns(50);
	    queueManagerDTO.setMaxMQOps(60);
	
	    ConfigDataTransferObject.QueueDTO queueDTO = new ConfigDataTransferObject.QueueDTO();
	    queueDTO.setErrorThreshold(3);
	    Map<String, ConfigDataTransferObject.QueueThresholdDTO> queueThresholds = new HashMap<>();
	    ConfigDataTransferObject.QueueThresholdDTO thresholdDTO = new ConfigDataTransferObject.QueueThresholdDTO();
	    thresholdDTO.setDepth(4);
	    thresholdDTO.setActivity(20);
	    queueThresholds.put("SAMPLE_QUEUE", thresholdDTO);
	    queueDTO.setQueueThresholds(queueThresholds);
	    
	    retrievedThresholdsDTO.setApps(appDTO);
	    retrievedThresholdsDTO.setQueue_manager(queueManagerDTO);
	    retrievedThresholdsDTO.setQueues(queueDTO);
	
	    dto.setRetrievedThresholds(retrievedThresholdsDTO);
	    return dto;
    }
    
    
}
