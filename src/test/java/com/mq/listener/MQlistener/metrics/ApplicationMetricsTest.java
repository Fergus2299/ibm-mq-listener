package com.mq.listener.MQlistener.metrics;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.models.Issue.Issue;
import com.mq.listener.MQlistener.testConfig.TestConfig;
import com.mq.listener.MQlistener.utils.IssueSender;


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
        
        applicationMetrics = new ApplicationMetrics(configManager, sender);
        ReflectionTestUtils.setField(applicationMetrics, "qMgrName", "QM1");
        ReflectionTestUtils.setField(configManager, "config", TestConfig.createSampleConfig1());
        applicationMetrics.setSender(sender); 
    }
    // test whether 1 above threshold makes correct error
    @Test
    @Order(1)
    void test_ConnectionPatternIssue_conns_above_threshold() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 101; i++) {
    		applicationMetrics.addMessage(TestConfig.sampleAccountingData1());
    	}
    	applicationMetrics.evaluateMetrics();
        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());

        // asserting that the created issue is too manay connections
        Issue sentIssue = issueCaptor.getValue();
        String generalDesc = sentIssue.getGeneralDesc();
        assertTrue(generalDesc.contains("Too many MQCONNs in time interval"), "The generalDesc does not contain the expected text");
    }
    // test whether connections to put get ratio issue works
    @Test
    @Order(2)
    void test_ConnectionPatternIssue_COR_Bad() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 20; i++) {
    		applicationMetrics.addMessage(TestConfig.sampleAccountingData1());
    	}
    	applicationMetrics.evaluateMetrics();
        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());
        // asserting that the created issue is that there is a bad connection to operations pattern
        Issue sentIssue = issueCaptor.getValue();
        String generalDesc = sentIssue.getGeneralDesc();
        assertTrue(generalDesc.contains("Ratio of MQCONNS to GETS/PUTS"), "The generalDesc does not contain the expected text");
    }
 
    
    
    // test whether 1 below threshold makes issue 
    @Test
    @Order(3)
    void test_ConnectionPatternIssue_conns_below_threshold() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 100; i++) {
    		applicationMetrics.addMessage(TestConfig.sampleAccountingData2());
    	}
    	applicationMetrics.evaluateMetrics();
        verify(sender, times(0)).sendIssue(any());

    }
    // test when COR ok
    @Test
    @Order(4)
    void test_ConnectionPatternIssue_COR_OK() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 20; i++) {
    		applicationMetrics.addMessage(TestConfig.sampleAccountingData2());
    	}
    	applicationMetrics.evaluateMetrics();
    	verify(sender, times(0)).sendIssue(any());
    }
    
    // test when COR ratio is bad, but min connections not met to see this as a problem
    @Test
    @Order(5)
    void test_ConnectionPatternIssue_COR_Bad_MinCons_Not_Met() throws Exception {
    	System.out.println(configManager.config);
    	for (int i = 0; i < 9; i++) {
    		applicationMetrics.addMessage(TestConfig.sampleAccountingData1());
    	}
    	applicationMetrics.evaluateMetrics();
    	verify(sender, times(0)).sendIssue(any());

    }
}
