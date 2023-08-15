package com.mq.listener.MQlistener;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;

@Component
public class QMGRListener {
    private static final Logger log = LoggerFactory.getLogger(QMGRListener.class);

    @JmsListener(destination = "SYSTEM.ADMIN.QMGR.EVENT")
    public void listen(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            try {
                MQMessage mqMsg = MQListener.convertToMQMessage((BytesMessage) receivedMessage);
                PCFMessage pcfMsg = new PCFMessage(mqMsg);
                processQMGRMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
    
    private void processQMGRMessage(PCFMessage pcfMsg) {
    	try {
			MQCFH cfh = pcfMsg.getHeader();
	        int eventReason = cfh.getReason();
	        if (eventReason == 2035) {
//	        	PCFParser.parsePCFMessage(pcfMsg);
	            String eventQueueName = pcfMsg.getStringParameterValue(MQConstants.MQCA_Q_NAME).trim();
//	            // TODO: countError needs to be thread safe
	            ErrorCounter.countError(eventQueueName, eventReason);
	        } else {
	            System.out.println("Recieved QMGR EVENT which was not a 2035!");
	        }
    	} catch (Exception e) {
    		log.error("Error processing PCF message", e);
    	}
    }
}