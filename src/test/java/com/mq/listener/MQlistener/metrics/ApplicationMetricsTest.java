package com.mq.listener.MQlistener.metrics;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.testConfig.TestConfig;
import com.mq.listener.MQlistener.utils.IssueSender;

import org.junit.jupiter.api.Test;

class ApplicationMetricsTest {
    
    @Mock
    private ConfigManager configManager;
    
    @Mock
    private IssueSender sender;

    @InjectMocks
    private ApplicationMetrics applicationMetrics;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(applicationMetrics, "qMgrName", "QM1");
        ReflectionTestUtils.setField(configManager, "config", TestConfig.createSampleConfig1());
        applicationMetrics.setSender(sender); 
    }
    
    
}
