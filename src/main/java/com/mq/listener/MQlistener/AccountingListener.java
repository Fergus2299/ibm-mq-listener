package com.mq.listener.MQlistener;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.JMSException;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.MQMessage;
import com.ibm.mq.constants.MQConstants;

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
                processAccountingMessage(pcfMsg);
            } catch (Exception e) {
                MQListener.logProcessingError(e, "PCF");
            }
        } else {
            log.warn("Received non-bytes message: {}", receivedMessage);
        }
    }
    
    private void processAccountingMessage(PCFMessage pcfMsg) {
    	try {
    		// for now we're just sending this to a file
            String parsedPCF = PCFParser.toPcfMessageJson(pcfMsg);
            // check what command it is
    		MQCFH cfh = pcfMsg.getHeader();
            int command = cfh.getCommand();
            String fileName;

            switch (command) {
                case 167: // MQCMD_ACCOUNTING_MQI
                    
                    // Note: we are assuming a model of only applications which put and get
                    // in reality the landscape of MQ is richer and there can be other calls e.g. MQIAMO_TOPIC_PUTS
                	
    	            // get the necessary data in one method
    	            AccountingData data = extractAccountingData(pcfMsg);
//    	            System.out.println(data.toString());
    	            AccountingMetrics.addMessage(data);
    	            
                    break;
                case 168: // MQCMD_ACCOUNTING_Q
//                    fileName = "AccountingQ.json";
                    break;
                default:
                    fileName = "UnknownAccounting.json"; // for any other command codes that you haven't explicitly handled
                    log.warn("Received an unhandled accounting command code: {}", command);
                    break;
            }
//            PCFParser.saveJsonToFile(parsedPCF, fileName);
    	} catch (Exception e) {
    		log.error("Error processing PCF message", e);
    	}
    }
    
    private AccountingData extractAccountingData(PCFMessage pcfMsg) throws Exception {
        AccountingData data = new AccountingData();
//        TODO: it says in the constants package documentation that MQIAMO_PUTS is an int
//        yet in my testing case it's a list so need to ask Mark Bluemel
        data.setUserIdentifier(pcfMsg.getStringParameterValue(MQConstants.MQCACF_USER_IDENTIFIER).trim());
        data.setStartDate(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_DATE).trim());
        data.setStartTime(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim());
        data.setEndDate(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_DATE).trim());
        data.setEndTime(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim());
        data.setPuts(pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUTS)[0]);
        data.setPutsFailed(pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUTS_FAILED));
        data.setPut1s(pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUT1S)[0]);
        data.setPut1sFailed(pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUT1S_FAILED));
        data.setGets(pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_GETS)[0]);
        data.setGetsFailed(pcfMsg.getIntParameterValue(MQConstants.MQIAMO_GETS_FAILED));

        return data;
    }
}