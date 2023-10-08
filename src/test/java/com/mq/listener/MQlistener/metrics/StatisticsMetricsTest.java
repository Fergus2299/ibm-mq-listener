package com.mq.listener.MQlistener.metrics;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.logging.StatisticsLogger;
import com.mq.listener.MQlistener.models.Issue.Issue;
import com.mq.listener.MQlistener.testConfig.TestConfig;
import com.mq.listener.MQlistener.utils.IssueSender;

class StatisticsMetricsTest {
 
    @Mock
    private ConfigManager configManager;
    @Mock
    private IssueSender sender;
    @Mock
    private StatisticsLogger logger;

    @InjectMocks
    private StatisticsMetrics statisticsMetrics;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        statisticsMetrics = new StatisticsMetrics(configManager, sender, logger);
        ReflectionTestUtils.setField(statisticsMetrics, "qMgrName", "QM1");
        ReflectionTestUtils.setField(configManager, "config", TestConfig.createSampleConfig1());
        statisticsMetrics.setSender(sender);
        statisticsMetrics.setLogger(logger);
        
    }
    
    // test whether connections QM works
    @Test
    @Order(1)
    void test_Activity_spike_QM_conns_above_threshold() throws Exception {
        
    	Map<String, Integer> statsForQM = new HashMap<>();
        statsForQM.put("CONNS", 201);
        statsForQM.put("CONNS_FAILED", 0);
        statsForQM.put("OPENS", 0);
        statsForQM.put("OPENS_FAILED", 0);
        statsForQM.put("PUTS", 500);
        statsForQM.put("PUTS_FAILED", 10);
        statsForQM.put("GETS", 480);
        statsForQM.put("GETS_FAILED", 8);
        
		statisticsMetrics.addQMTimeSeriesStats(
				LocalTime.of(9, 30),
				LocalTime.of(9, 31),
				statsForQM
				);
		ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());
        Issue sentIssue = issueCaptor.getValue();
        // ensure sent issue is right type of ErrorSpike:
        System.out.println(sentIssue.getGeneralDesc());
        assertTrue(sentIssue.getGeneralDesc().contains("connections"));
        assertEquals("<QMGR>", sentIssue.getMQObjectType());
    }
    
    // test whether operations QM works
    @Test
    @Order(2)
    void test_Activity_spike_QM_cons_below_threshold_ops_above_threshold() throws Exception {
    	
    	Map<String, Integer> statsForQM = new HashMap<>();
        statsForQM.put("CONNS", 200);
        statsForQM.put("CONNS_FAILED", 0);
        statsForQM.put("OPENS", 0);
        statsForQM.put("OPENS_FAILED", 0);
        statsForQM.put("PUTS", 500);
        statsForQM.put("PUTS_FAILED", 10);
        statsForQM.put("GETS", 480);
        statsForQM.put("GETS_FAILED", 8);
        
		statisticsMetrics.addQMTimeSeriesStats(
				LocalTime.of(9, 30),
				LocalTime.of(9, 31),
				statsForQM
				);
		ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());
        Issue sentIssue = issueCaptor.getValue();
        // ensure right kind of issue
        assertTrue(sentIssue.getGeneralDesc().contains("operations"));
        assertEquals("<QMGR>", sentIssue.getMQObjectType());
    }
    
    // test whether breaching niether works for QM
    @Test
    @Order(3)
    void test_Activity_spike_conns_below_threshold_ops_below_threshold() throws Exception {
    	Map<String, Integer> statsForQM = new HashMap<>();
        statsForQM.put("CONNS", 200);
        statsForQM.put("CONNS_FAILED", 0);
        statsForQM.put("OPENS", 0);
        statsForQM.put("OPENS_FAILED", 0);
        statsForQM.put("PUTS", 100);
        statsForQM.put("PUTS_FAILED", 100);
        statsForQM.put("GETS", 0);
        statsForQM.put("GETS_FAILED", 0); // 400 ops total (lower than 500)
        
		statisticsMetrics.addQMTimeSeriesStats(
				LocalTime.of(9, 30),
				LocalTime.of(9, 31),
				statsForQM
				);

        verify(sender, times(0)).sendIssue(any());
    }
    // test whether operations Q works
    @Test
    @Order(4)
    void test_Activity_spike_Q_ops() throws Exception {
        Map<String, Map<String, Integer>> queueStatsMap = new HashMap<>();

    	Map<String, Integer> queue1Stats = new HashMap<>();
        queue1Stats.put("PUTS", 100);
        queue1Stats.put("GETS", 0);
        queue1Stats.put("PUTS_FAILED", 101);
        queue1Stats.put("GETS_FAILED", 0);
        queueStatsMap.put("Queue1", queue1Stats);
        Map<String, Integer> queue2Stats = new HashMap<>();
        queue2Stats.put("PUTS", 5);
        queue2Stats.put("GETS", 10);
        queue2Stats.put("PUTS_FAILED", 101);
        queue2Stats.put("GETS_FAILED", 0);
        queueStatsMap.put("Queue2", queue2Stats);
        
		statisticsMetrics.addQTimeSeriesStats(
				LocalTime.of(9, 30),
				LocalTime.of(9, 31),
				queueStatsMap
				);
		// only QUEUE.1 should have an issue created
		ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(sender, times(1)).sendIssue(issueCaptor.capture());
        Issue sentIssue = issueCaptor.getValue();
        assertEquals("<QUEUE>", sentIssue.getMQObjectType());
        assertEquals("Queue1", sentIssue.getMQObjectName());
    }
}
