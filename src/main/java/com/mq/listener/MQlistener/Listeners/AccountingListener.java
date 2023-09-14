package com.mq.listener.MQlistener.Listeners;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.processors.AccountingProcessor;
import com.ibm.mq.MQMessage;

@Component
public class AccountingListener {

    private static final Logger log = LoggerFactory.getLogger(AccountingListener.class);

    @JmsListener(destination = "SYSTEM.ADMIN.ACCOUNTING.QUEUE")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                
                // send PCF message for processing
                AccountingProcessor.processAccountingMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}