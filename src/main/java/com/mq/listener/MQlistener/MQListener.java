package com.mq.listener.MQlistener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.MQMessage;

@Component
public class MQListener {

    private static final Logger log = LoggerFactory.getLogger(MQListener.class);

    @JmsListener(destination = "SYSTEM.ADMIN.QMGR.EVENT")
    public void receive(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) receivedMessage;
            byte[] bytesreceived = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(bytesreceived);

            // convert to MQMessage then to PCFMessage
            MQMessage mqMsg = new MQMessage();
            try {
                mqMsg.write(bytesreceived);
                mqMsg.encoding = receivedMessage.getIntProperty("JMS_IBM_Encoding");
                mqMsg.format = receivedMessage.getStringProperty("JMS_IBM_Format");
                mqMsg.seek(0);

                PCFMessage pcfMsg = new PCFMessage(mqMsg);
//                String parsedOutput = PCFParser.parsePCFMessage(pcfMsg);
//                log.info("Parsed PCF message:\n{}", parsedOutput);
                
                log.info("Listener received PCF message: {}", pcfMsg);
            } catch (Exception e) {
                log.error("Error processing PCF message", e);
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}