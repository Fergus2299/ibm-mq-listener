package com.mq.listener.MQlistener.Listeners;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.processors.StatisticsProcessor;
import com.ibm.mq.MQMessage;

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
                System.out.println("recieved stats Message!");
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
