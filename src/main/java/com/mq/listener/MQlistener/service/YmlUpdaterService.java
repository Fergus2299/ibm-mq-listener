//package com.mq.listener.MQlistener.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Service;
//import org.yaml.snakeyaml.DumperOptions;
//import org.yaml.snakeyaml.Yaml;
//import org.yaml.snakeyaml.nodes.Tag;
//
//import com.mq.listener.MQlistener.config.AppConfig;
//import com.mq.listener.MQlistener.config.QueueConfig;
//import com.mq.listener.MQlistener.config.QueueManagerConfig;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.FileWriter;
//import java.util.Map;
//
//@Service
//public class YmlUpdaterService {
//
//    private final AppConfig appConfig;
//    private final QueueConfig queueConfig;
//    private final QueueManagerConfig queueManagerConfig;
//
//    @Autowired
//    public YmlUpdaterService(AppConfig appConfig, QueueConfig queueConfig, QueueManagerConfig queueManagerConfig) {
//    	
//        this.appConfig = appConfig;
//        this.queueConfig = queueConfig;
//        this.queueManagerConfig = queueManagerConfig;
//    }
//    public void updateConfig() throws Exception {
//    	
//    	System.out.println("Updating YML");
//    	// getting config section of current yml
//        Yaml yaml = new Yaml();
//        String projectRootPath = new File("").getAbsolutePath();
//
//        File file =  new File(projectRootPath, "src/main/resources/application.yml");
//        System.out.println("Reading from: " + file.getAbsolutePath());
//
//        Map<String, Object> yamlContent;
//        try (FileInputStream input = new FileInputStream(file)) {
//            yamlContent = yaml.load(input);
//        }
//
//        Map<String, Object> config = (Map<String, Object>) yamlContent.get("config");
//        System.out.println("Config: " + config);
//        
//        Map<String, Object> app = (Map<String, Object>) config.get("app");
//        Map<String, Object> connections = (Map<String, Object>) app.get("connections");
//        connections.put("max", appConfig.getConnections().getMax());
//        app.put("connectionOperationsRatio", appConfig.getConnectionOperationsRatio());
//        
//        // Update queue manager config
//        Map<String, Object> queueManager = (Map<String, Object>) config.get("queue-manager");
//        
//        Map<String, Object> qmConnections = (Map<String, Object>) queueManager.get("connections");
//        qmConnections.put("max", queueManagerConfig.getConnections().getMax());
//        
//        Map<String, Object> qmOperations = (Map<String, Object>) queueManager.get("operations");
//        qmOperations.put("max", queueManagerConfig.getOperations().getMax());
//        
//        Map<String, Object> qmErrors = (Map<String, Object>) queueManager.get("errors");
//        qmErrors.put("max", queueManagerConfig.getErrors().getMax());
//        
//     // Update queue config
//        Map<String, Object> queue = (Map<String, Object>) config.get("queue");
//        
//        Map<String, Object> queueErrors = (Map<String, Object>) queue.get("errors");
//        queueErrors.put("max", queueConfig.getErrors().getMax());
//        
//        queue.put("operationsDefault", queueConfig.getOperationsDefault());
//        queue.put("operationsSpecificQueues", queueConfig.getOperationsSpecificQueues());
//        System.out.println("Config after updates: " + config);
//        
//        // Define DumperOptions for formatted output
//        DumperOptions options = new DumperOptions();
//        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
//        options.setPrettyFlow(true);
//        yaml = new Yaml(options);
//        
//        System.out.println("Writing to: " + file.getAbsolutePath());
//        try (FileWriter writer = new FileWriter(file)) {
//            yaml.dump(yamlContent, writer);
//        }
//    }
//    
//}