package com.mq.listener.MQlistener.Listeners;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.MQCFGR;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.mq.listener.MQlistener.parsers.PCFParser;
import com.mq.listener.MQlistener.processors.StatisticsProcessor;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;

@Component
public class StatisticsListener {
    
    private static final Logger log = LoggerFactory.getLogger(StatisticsListener.class);

    private final StatisticsProcessor statisticsProcessor;
    
    public StatisticsListener(StatisticsProcessor statisticsProcessor) {
        this.statisticsProcessor = statisticsProcessor;
    }

    @JmsListener(destination = "SYSTEM.ADMIN.STATISTICS.QUEUE")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                // TODO: assuming StatQ message is the only stats message being produced by MQ
                statisticsProcessor.processStatisticsMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }

}
