package com.mq.listener.MQlistener.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.models.Issue.Issue;
import com.mq.listener.MQlistener.testConfig.TestConfig;
import com.mq.listener.MQlistener.utils.IssueSender;

@ExtendWith(MockitoExtension.class)
class ErrorMetricsTest {
	    @Mock
	    private ConfigManager configManager;
	
	    @Mock
	    private IssueSender sender;
	
	    @InjectMocks
	    private ErrorMetrics errorMetrics;
	    
        String userId = "500";
        String appName = "app";
        String channelName = "something";
        String connName = "27.40.12.195";
        String CSPUserId = "something";
        String queueName = "Queue1";
        
	    @BeforeEach
	    void setUp() {
	        MockitoAnnotations.openMocks(this);
	        
	        errorMetrics = new ErrorMetrics(configManager, sender);
	        ReflectionTestUtils.setField(errorMetrics, "qMgrName", "QM1");
	        ReflectionTestUtils.setField(configManager, "qMgrName", "QM1");
	        ReflectionTestUtils.setField(configManager, "config", TestConfig.createSampleConfig1());
	        // sender needs to be manually set for some reason
	        errorMetrics.setSender(sender); 
	    }
	    
	    // the following test evaluate metrics when count is 1 above threshold
	    @Test
	    @Order(1)
	    void test_ErrorSpike_2035_QM_above_threshold() throws Exception {
	        for (int i = 0; i < 11; i++) {
	        	errorMetrics.countType1AuthError(userId, appName, channelName, connName, CSPUserId);
	        }
	        
	        errorMetrics.evaluateMetrics();
	        // ensure issue sent
	        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
	        verify(sender, times(1)).sendIssue(issueCaptor.capture());
	        Issue sentIssue = issueCaptor.getValue();
	        // ensure sent issue is right type of ErrorSpike:
	        assertEquals("Too_Many_2035s", sentIssue.getIssueCode());
	        assertEquals("<QMGR>", sentIssue.getMQObjectType());
	    }
	    
	    @Test
	    @Order(2)
	    void test_ErrorSpike_2035_Q_above_threshold() throws Exception {
	        for (int i = 0; i < 6; i++) {
	        	errorMetrics.countType2AuthError(userId, appName, queueName);
	        }
	        errorMetrics.evaluateMetrics();
	        // ensure issue sent
	        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
	        verify(sender, times(1)).sendIssue(issueCaptor.capture());
	        Issue sentIssue = issueCaptor.getValue();
	        // ensure sent issue is right type of ErrorSpike:
	        assertEquals("Too_Many_2035s", sentIssue.getIssueCode());
	        assertEquals("<QUEUE>", sentIssue.getMQObjectType());
	    }
	    
	    @Test
	    @Order(3)
	    void test_ErrorSpike_2085_QM_above_threshold() throws Exception {

	        for (int i = 0; i < 11; i++) {
	        	errorMetrics.countUnknownObjectError(appName, connName, channelName, queueName);
	        }
	        errorMetrics.evaluateMetrics();

	        // ensure issue sent
	        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
	        verify(sender, times(1)).sendIssue(issueCaptor.capture());
	        Issue sentIssue = issueCaptor.getValue();
	        // ensure sent issue is right type of ErrorSpike:
	        assertEquals("Too_Many_2085s", sentIssue.getIssueCode());

	    }
	    
	    // the following test evaluate metrics when count is 1 below threshold
	    @Test
	    @Order(4)
	    void test_ErrorSpike_2035_QM_below_threshold() throws Exception {
	        for (int i = 0; i < 10; i++) {
	        	errorMetrics.countType1AuthError(userId, appName, channelName, connName, CSPUserId);
	        }
	        
	        errorMetrics.evaluateMetrics();
	        // verify that 3 issues have been sent to frontend
	        verify(sender, times(0)).sendIssue(any());
	    }
	    
	    @Test
	    @Order(5)
	    void test_ErrorSpike_2035_Q_below_threshold() throws Exception {
	        for (int i = 0; i < 5; i++) {
	        	errorMetrics.countType2AuthError(userId, appName, queueName);
	        }
	        
	        errorMetrics.evaluateMetrics();
	        // verify that 3 issues have been sent to frontend
	        verify(sender, times(0)).sendIssue(any());
	    }
	    
	    @Test
	    @Order(6)
	    void test_ErrorSpike_2085_QM_below_threshold() throws Exception {

	        for (int i = 0; i < 10; i++) {
	        	errorMetrics.countUnknownObjectError(appName, connName, channelName, queueName);
	        }
	        
	        errorMetrics.evaluateMetrics();
	        // verify that 3 issues have been sent to frontend
	        verify(sender, times(0)).sendIssue(any());
	    }
}
