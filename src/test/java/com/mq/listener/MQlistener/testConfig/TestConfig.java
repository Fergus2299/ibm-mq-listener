package com.mq.listener.MQlistener.testConfig;

import java.util.HashMap;
import java.util.Map;

import com.mq.listener.MQlistener.config.Config;
import com.mq.listener.MQlistener.config.ConfigDTO;
import com.mq.listener.MQlistener.models.AccountingData;

public class TestConfig {
	
    public static Config createSampleConfig1() {
        Config config = new Config();
        Config.QMConfig qmConfig = new Config.QMConfig();

        // App Configurations
        Config.QMConfig.AppConfig appConfig = new Config.QMConfig.AppConfig();
        Config.QMConfig.AppConfig.ConnectionConfig appConnectionConfig = new Config.QMConfig.AppConfig.ConnectionConfig();
        appConnectionConfig.setMax(100);
        appConfig.setConnections(appConnectionConfig);

        Config.QMConfig.AppConfig.ConnectionOperationsRatioConfig connectionOperationsRatioConfig = new Config.QMConfig.AppConfig.ConnectionOperationsRatioConfig();
        connectionOperationsRatioConfig.setMax(0.8);
        connectionOperationsRatioConfig.setConnections(10);
        appConfig.setConnectionOperationsRatio(connectionOperationsRatioConfig);
        
        // Queue Manager Configurations
        Config.QMConfig.QueueManagerConfig queueManagerConfig = new Config.QMConfig.QueueManagerConfig();
        Config.QMConfig.QueueManagerConfig.ConnectionConfig qmConnectionConfig = new Config.QMConfig.QueueManagerConfig.ConnectionConfig();
        qmConnectionConfig.setMax(200);
        queueManagerConfig.setConnections(qmConnectionConfig);
        
        Config.QMConfig.QueueManagerConfig.OperationsConfig operationsConfig = new Config.QMConfig.QueueManagerConfig.OperationsConfig();
        operationsConfig.setMax(500);
        queueManagerConfig.setOperations(operationsConfig);
        
        Config.QMConfig.QueueManagerConfig.ErrorsConfig errorsConfig = new Config.QMConfig.QueueManagerConfig.ErrorsConfig();
        errorsConfig.setMax(10);
        queueManagerConfig.setErrors(errorsConfig);
        
        // Queue Configurations
        Config.QMConfig.QueueConfig queueConfig = new Config.QMConfig.QueueConfig();
        Config.QMConfig.QueueConfig.ErrorsConfig queueErrorsConfig = new Config.QMConfig.QueueConfig.ErrorsConfig();
        queueErrorsConfig.setMax(5); 
        queueConfig.setErrors(queueErrorsConfig);
        queueConfig.setOperationsDefault(Config.OPERATIONS_DEFAULT);
        queueConfig.setOperationsSpecificQueues(Map.of("Queue1", 200, "Queue2", 400)); // set specific operations for queues
        
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
    
    
    
    public static ConfigDTO createSampleDto1() {
	    ConfigDTO dto = new ConfigDTO();
	
	    ConfigDTO.RetrievedThresholdsDTO retrievedThresholdsDTO = new ConfigDTO.RetrievedThresholdsDTO();
	    
	    ConfigDTO.AppDTO appDTO = new ConfigDTO.AppDTO();
	    appDTO.setConnThreshold(10);
	    appDTO.setConnOpRatioThreshold(0.5f);
	    appDTO.setMinimumConns(4);
	    
	    ConfigDTO.QueueManagerDTO queueManagerDTO = new ConfigDTO.QueueManagerDTO();
	    queueManagerDTO.setErrorThreshold(2);
	    queueManagerDTO.setMaxMQConns(50);
	    queueManagerDTO.setMaxMQOps(60);
	
	    ConfigDTO.QueueDTO queueDTO = new ConfigDTO.QueueDTO();
	    queueDTO.setErrorThreshold(3);
	    Map<String, ConfigDTO.QueueThresholdDTO> queueThresholds = new HashMap<>();
	    ConfigDTO.QueueThresholdDTO thresholdDTO = new ConfigDTO.QueueThresholdDTO();
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
    
    public static AccountingData sampleAccountingData1() {
    	
        AccountingData sample = new AccountingData();

        sample.setUserIdentifier("app1");
        sample.setAppName("SampleApp");
        sample.setConnName("SampleConnection");
        sample.setStartDate("2023-10-03");
        sample.setStartTime("12:00:00");
        sample.setEndDate("2023-10-03");
        sample.setEndTime("13:00:00");
        sample.setPuts(1);
        sample.setPutsFailed(0);
        sample.setPut1s(0);
        sample.setPut1sFailed(0);
        sample.setGets(0);
        sample.setGetsFailed(0);

        return sample;
    }
    public static AccountingData sampleAccountingData2() {
    	
        AccountingData sample = new AccountingData();

        sample.setUserIdentifier("app2");
        sample.setAppName("SampleApp");
        sample.setConnName("SampleConnection");
        sample.setStartDate("2023-10-03");
        sample.setStartTime("12:00:00");
        sample.setEndDate("2023-10-03");
        sample.setEndTime("13:00:00");
        sample.setPuts(1);
        sample.setPutsFailed(0);
        sample.setPut1s(0);
        sample.setPut1sFailed(0);
        sample.setGets(0);
        sample.setGetsFailed(0);

        return sample;
    }
    

}
