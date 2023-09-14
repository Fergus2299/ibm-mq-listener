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
import com.ibm.mq.MQMessage;

@Component
public class QMGRListener {
    private static final Logger log = LoggerFactory.getLogger(QMGRListener.class);

    @JmsListener(destination = "SYSTEM.ADMIN.QMGR.EVENT")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
//                System.out.println(pcfMsg);
                QMGRProcessor.processQMGRMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}