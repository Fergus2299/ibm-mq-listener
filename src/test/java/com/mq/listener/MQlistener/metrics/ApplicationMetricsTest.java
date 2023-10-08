package com.mq.listener.MQlistener.metrics;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.models.Issue.Issue;
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
    // test whether too many connections works
    @Test
    @Order(1)
    void testApplicationMetrics_TooManyConnections() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 500; i++) {
    		ApplicationMetrics.addMessage(TestConfig.sampleAccountingData1());
    	}
    	applicationMetrics.evaluateMetrics();
        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());

        // asserting that the created issue is too manay connections
        Issue sentIssue = issueCaptor.getValue();
        String generalDesc = sentIssue.getGeneralDesc();
        assertTrue(generalDesc.contains("Too many MQCONNs in time interval"), "The generalDesc does not contain the expected text");
    }
    // test whether connections to put get ration issue works
    @Test
    @Order(2)
    void testApplicationMetrics_BadConnectionPattern() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 20; i++) {
    		ApplicationMetrics.addMessage(TestConfig.sampleAccountingData2());
    	}
    	applicationMetrics.evaluateMetrics();
        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());
        // asserting that the created issue is that there is a bad connection to operations pattern
        Issue sentIssue = issueCaptor.getValue();
        String generalDesc = sentIssue.getGeneralDesc();
        assertTrue(generalDesc.contains("Ratio of MQCONNS to GETS/PUTS"), "The generalDesc does not contain the expected text");
    }
}
