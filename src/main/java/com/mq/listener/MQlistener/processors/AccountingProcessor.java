package com.mq.listener.MQlistener.processors;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.MQCFH;
import com.mq.listener.MQlistener.metrics.ApplicationMetrics;
import com.mq.listener.MQlistener.models.AccountingData;
import com.mq.listener.MQlistener.parsers.PCFParser;

public class AccountingProcessor {
	private static final Logger log = LoggerFactory.getLogger(AccountingProcessor.class);
    static public void processAccountingMessage(PCFMessage pcfMsg) {
    	
	try {
        // check what command it is
		MQCFH cfh = pcfMsg.getHeader();
        int command = cfh.getCommand();

        switch (command) {
            case 167: // MQCMD_ACCOUNTING_MQI
                
                // Note: we are assuming a model of only applications which put and get
                // in reality the landscape of MQ is richer and there can be other calls e.g. MQIAMO_TOPIC_PUTS
	            // creating the AccoutingData object
	            AccountingData data = extractAccountingData(pcfMsg);
	            // passing on the message
	            ApplicationMetrics.addMessage(data);
                break;
            default:
            	log.info("Received an unhandled accounting command code: " + command);
                break;
        }
	} catch (Exception e) {
		log.error("Error processing accounting PCF message" + e);
		PCFParser.parsePCFMessage(pcfMsg);
	}
}
	
    private static AccountingData extractAccountingData(PCFMessage pcfMsg) throws Exception {
        AccountingData data = new AccountingData();
        // TODO: error handling
        data.setUserIdentifier(pcfMsg.getStringParameterValue(MQConstants.MQCACF_USER_IDENTIFIER).trim());
        data.setAppName(pcfMsg.getStringParameterValue(MQConstants.MQCACF_APPL_NAME).trim());
        data.setStartDate(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_DATE).trim());
        data.setStartTime(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim());
        data.setEndDate(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_DATE).trim());
        data.setEndTime(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim());
        try {
            String connName = pcfMsg.getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME).trim();
            data.setConnName(connName);
        } catch (Exception e) {
            data.setConnName("");
            log.warn("Failure getting MQCACH_CONNECTION_NAME data.");
        }

        // lists contain data for nonpersistent and persistent messages so we sum them to get total
        // For MQIAMO_PUTS
        try {
            int[] putsArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUTS);
            data.setPuts((putsArray != null) ? Arrays.stream(putsArray).sum() : 0);
        } catch (Exception e) {
        	log.warn("Failure getting MQIAMO_PUTS data.");
            data.setPuts(0);
        }

        // For MQIAMO_PUT1S
        try {
            int[] put1sArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUT1S);
            data.setPut1s((put1sArray != null) ? Arrays.stream(put1sArray).sum() : 0);
        } catch (Exception e) {
        	log.warn("Failure getting MQIAMO_PUT1S data.");
            data.setPut1s(0);
        }

        // For MQIAMO_GETS
        try {
            int[] getsArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_GETS);
            data.setGets((getsArray != null) ? Arrays.stream(getsArray).sum() : 0);
        } catch (Exception e) {
        	log.warn("Failure getting MQIAMO_GETS data.");
            data.setGets(0);
        }
        
        
        // getting the integer values
        try {
            Integer putsFailedValue = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUTS_FAILED);
            data.setPutsFailed(putsFailedValue);
        } catch (Exception e) {
            data.setPutsFailed(0);
            log.warn("Could not get putsfailed data for Accouting Message");
        }
        
        try {
            Integer setPut1sFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUT1S_FAILED);
            data.setPut1sFailed(setPut1sFailed);
        } catch (Exception e) {
            data.setPut1sFailed(0);
            log.warn("Could not get put1sfailed data for Accouting Message");
        }
        
        try {
            Integer setGetsFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_GETS_FAILED);
            data.setGetsFailed(setGetsFailed);
        } catch (Exception e) {
            data.setGetsFailed(0);
            log.warn("Could not get getsfailed data for Accouting Message");
        }


        return data;
    }
}
