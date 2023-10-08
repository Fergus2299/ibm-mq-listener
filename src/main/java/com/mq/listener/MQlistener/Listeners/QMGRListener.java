package com.mq.listener.MQlistener.Listeners;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.processors.QMGRProcessor;
import com.mq.listener.MQlistener.processors.StatisticsProcessor;
import com.ibm.mq.MQMessage;

@Component
public class QMGRListener {
    private static final Logger logger = LoggerFactory.getLogger(QMGRListener.class);

    private final QMGRProcessor qmgrProcessor;
    
    public QMGRListener(QMGRProcessor qmgrProcessor) {
        this.qmgrProcessor = qmgrProcessor;
    }
    @JmsListener(destination = "SYSTEM.ADMIN.QMGR.EVENT")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = ListenerUtilities.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                System.out.println("recieved Qmgr Message!");
                qmgrProcessor.processQMGRMessage(pcfMsg);
            } catch (Exception e) {
                ListenerUtilities.logProcessingError(e, "PCF");
            }
        } else {
        	logger.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}