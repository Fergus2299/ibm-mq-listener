package com.mq.listener.MQlistener.config;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mq.listener.MQlistener.config.Config.QMConfig;
import com.mq.listener.MQlistener.testConfig.TestConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ConfigManagerTest {
	
    @InjectMocks
    private ConfigManager configManager;
    @Mock
    private ObjectMapper objectMapper;


    protected static final String BASE_PATH = "config/";
	
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        ReflectionTestUtils.setField(configManager, "qMgrName", "QM1");
    }
    @Test
    @Order(1)
    public void testConfigLoading() throws Exception {
    	
    	configManager.init();
        Config loadedConfig = configManager.getConfig();

        assertNotNull(loadedConfig);
        
        Config.QMConfig qmConfig = loadedConfig.getQms().get("<DEFAULT>");
        assertNotNull(qmConfig);
        assertNotNull(qmConfig.getApp().getConnections().getMax());
    }
    
    @Test
    @Order(2)
    public void testConfigSaving() throws Exception {

    	Path baseDirectory = Paths.get("").toAbsolutePath();
    	Path configFilePath = baseDirectory.resolve(BASE_PATH).resolve("config.json");
    	Path backupFilePath = baseDirectory.resolve(BASE_PATH).resolve("configBackup.json");
        Files.copy(configFilePath, backupFilePath);
    	
        // Create a sample config and populate it
        Config mockConfig = TestConfig.createSampleConfig1();
        ReflectionTestUtils.setField(configManager, "config", mockConfig);
        
        configManager.saveConfigToFile("config");
        configManager.init();
        Config loadedConfig = configManager.getConfig();

        System.out.println(loadedConfig.toString());
        String mockConfigJson = new ObjectMapper().writeValueAsString(mockConfig);
        String loadedConfigJson = new ObjectMapper().writeValueAsString(loadedConfig);
        assertEquals(mockConfigJson, loadedConfigJson);
        
        // restore old config file
        Files.copy(backupFilePath, configFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        Files.deleteIfExists(backupFilePath);
    }
    @Test
    @Order(3)
    public void testLoadingCorruptedConfigThrowsError() throws Exception {
        // prepare corrupted config file
    	Path baseDirectory = Paths.get("").toAbsolutePath();
    	Path configFilePath = baseDirectory.resolve(BASE_PATH).resolve("config.json");
    	Path backupFilePath = baseDirectory.resolve(BASE_PATH).resolve("configBackup.json");
    	Files.copy(configFilePath, backupFilePath);
        Files.write(configFilePath, "{ corrupted json }".getBytes());
        
        
        Exception thrownException = null;

        try {
        	configManager.init();
        	// we expect this to happen
        } catch (RuntimeException e) {
	        thrownException = e;
	    } finally {
        	Files.copy(backupFilePath, configFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(backupFilePath);
          
        }
        assertNotNull(thrownException, "Expected exception to be thrown");
    
    }
    // test tha config can 
    @Test
    @Order(4)
    public void testUpdatingConfigInMemory() throws Exception {
    	Path baseDirectory = Paths.get("").toAbsolutePath();
    	Path configFilePath = baseDirectory.resolve(BASE_PATH).resolve("config.json");
    	Path backupFilePath = baseDirectory.resolve(BASE_PATH).resolve("configBackup.json");
        Files.copy(configFilePath, backupFilePath);
    	
    	configManager.config = TestConfig.createSampleConfig1();
    	ConfigDTO dto = TestConfig.createSampleDto1();
    	configManager.updateConfigurations(dto);
    	Config updatedConfig = configManager.getConfig();
    	QMConfig queueManagerConfig = 
    			updatedConfig
                .getQms()
                .get("QM1");
    	// asserting that certain sample values have been changed - this should be sufficient
    	// won't check all values
    	  assertEquals(0.5f, queueManagerConfig.getApp().getConnectionOperationsRatio().getMax(), 0.01); // tolerance added for float comparison
    	  assertEquals(4, queueManagerConfig.getApp().getConnectionOperationsRatio().getConnections());
    	  assertEquals(10, queueManagerConfig.getApp().getConnections().getMax());
    	  
          // restore old config file
          Files.copy(backupFilePath, configFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          Files.deleteIfExists(backupFilePath);
    	
    }

    
}
