package com.mq.listener.MQlistener.Listeners;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.processors.PerformanceProcessor;
import com.ibm.mq.MQMessage;

@Component
public class PerformanceListener {
    private static final Logger log = LoggerFactory.getLogger(PerformanceListener.class);
    
    private final PerformanceProcessor performanceProcessor;

    public PerformanceListener(PerformanceProcessor performanceProcessor) {
        this.performanceProcessor = performanceProcessor;
    }
    @JmsListener(destination = "SYSTEM.ADMIN.PERFM.EVENT")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                // send PCF message for processing
                System.out.println("recieved performance Message!");
                performanceProcessor.processPerformanceMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}