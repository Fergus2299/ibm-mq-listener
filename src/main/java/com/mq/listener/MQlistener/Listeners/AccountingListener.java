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
import com.mq.listener.MQlistener.processors.PerformanceProcessor;
import com.ibm.mq.MQMessage;

@Component
public class AccountingListener {

    private static final Logger logger = LoggerFactory.getLogger(AccountingListener.class);
    
    private final AccountingProcessor accountingProcessor;

    public AccountingListener(AccountingProcessor accountingProcessor) {
        this.accountingProcessor = accountingProcessor;
    }
    
    @JmsListener(destination = "SYSTEM.ADMIN.ACCOUNTING.QUEUE")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = ListenerUtilities.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                logger.info("recieved Accounting Message!");
                System.out.println("recieved Accounting Message!");
                // send PCF message for processing
                accountingProcessor.processAccountingMessage(pcfMsg);
            } catch (Exception e) {
                ListenerUtilities.logProcessingError(e, "PCF");
            }
        } else {
        	logger.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}