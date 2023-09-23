package com.mq.listener.MQlistener.Listeners;

import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;

import com.ibm.mq.MQMessage;


//TODO: We only want to detect errors while the tool is turned on, it might be advisable to
// delete everything off the event queues when the app starts. - Might be worth advising IBM MQ
// to include date-time for events because of this because then we could have used this information.

//TODO:Threading

public class MQListener {
    private static final Logger log = LoggerFactory.getLogger(MQListener.class);

    public static MQMessage convertToMQMessage(BytesMessage bytesMessage) throws JMSException, IOException {
        byte[] bytesReceived = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(bytesReceived);
        MQMessage mqMsg = new MQMessage();
        mqMsg.write(bytesReceived);
        mqMsg.encoding = bytesMessage.getIntProperty("JMS_IBM_Encoding");
        mqMsg.format = bytesMessage.getStringProperty("JMS_IBM_Format");
        mqMsg.seek(0);
        return mqMsg;
    }
    
    public static void logProcessingError(Exception e, String context) {
        log.error("Error processing {} message", context, e);
    }
}