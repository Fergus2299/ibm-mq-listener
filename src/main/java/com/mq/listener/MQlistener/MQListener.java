package com.mq.listener.MQlistener;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;
import com.ibm.mq.headers.pcf.MQCFGR;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.MQCFIN;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;

@Component
public class MQListener {
    private static final Logger log = LoggerFactory.getLogger(MQListener.class);

    @JmsListener(destination = "SYSTEM.ADMIN.QMGR.EVENT")
    public void receive(Message receivedMessage) throws JMSException {
        if (receivedMessage instanceof BytesMessage) {
        	
        	// getting the BytesMesage
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
                // method for logging output
                PCFParser.parsePCFMessage(pcfMsg);

			    
                // getting the header info into a json and creating file
                String eventHeaderJson = PCFParser.toPcfMessageJson(pcfMsg);
                try (FileWriter file = new FileWriter("eventHeader.json")) {
                    file.write(eventHeaderJson);
                    System.out.println("Successfully written JSON to " + "eventHeader.json");
                } catch (IOException e) {
                    System.out.println("An error occurred while writing to the file: " + e.getMessage());
                }
                
            } catch (Exception e) {
                log.error("Error processing PCF message", e);
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
}