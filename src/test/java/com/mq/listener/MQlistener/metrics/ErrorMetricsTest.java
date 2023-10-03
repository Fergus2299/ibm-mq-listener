package com.mq.listener.MQlistener.metrics;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.testConfig.TestConfig;
import com.mq.listener.MQlistener.utils.IssueSender;

class ErrorMetricsTest {
	    @Mock
	    private ConfigManager configManager;
	
	    @Mock
	    private IssueSender sender;
	
	    @InjectMocks
	    private ErrorMetrics errorMetrics;
	    

	    @BeforeEach
	    void setUp() {
	        MockitoAnnotations.openMocks(this);
	        ReflectionTestUtils.setField(errorMetrics, "qMgrName", "QM1");
	        ReflectionTestUtils.setField(configManager, "qMgrName", "QM1");
	        ReflectionTestUtils.setField(configManager, "config", TestConfig.createSampleConfig1());
	        // sender needs to be manually set for some reason
	        errorMetrics.setSender(sender); 
	    }
	    
	    @Test
	    void testEvaluateMetrics() throws Exception {
	    	
	    	System.out.println(errorMetrics.sender);
	        String userId = "500";
	        String appName = "app";
	        String channelName = "something";
	        String connName = "27.40.12.195";
	        String CSPUserId = "something";
	        String queueName = "Queue1";
	        // loop 500 times - should create 3 issues
	        for (int i = 0; i < 500; i++) {
	        	ErrorMetrics.countType1AuthError(userId, appName, channelName, connName, CSPUserId);
	        	ErrorMetrics.countType2AuthError(userId, appName, queueName);
	        	ErrorMetrics.countUnknownObjectError(appName, connName, channelName, queueName);
	        }
	        
	        errorMetrics.evaluateMetrics();
	        // verify that 3 issues have been sent to frontend
	        verify(sender, times(3)).sendIssue(any());
	        

	    }
	    

}
