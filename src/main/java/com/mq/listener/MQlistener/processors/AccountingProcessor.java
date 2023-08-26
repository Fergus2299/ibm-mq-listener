package com.mq.listener.MQlistener.processors;

import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.MQCFH;
import com.mq.listener.MQlistener.issue_makers.AccountingMetrics;
import com.mq.listener.MQlistener.models.AccountingData;
import com.mq.listener.MQlistener.parsers.PCFParser;

public class AccountingProcessor {
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
//    	            System.out.println(data.toString());
	            
	            // passing on the message
	            AccountingMetrics.addMessage(data);
                break;
            case 168: // MQCMD_ACCOUNTING_Q
//                    fileName = "AccountingQ.json";
                break;
            default:
                System.out.println("Received an unhandled accounting command code: " + command);
                break;
        }
//            PCFParser.saveJsonToFile(parsedPCF, fileName);
	} catch (Exception e) {
		System.out.println("Error processing accounting PCF message" + e);
		PCFParser.parsePCFMessage(pcfMsg);
	}
}
	
    private static AccountingData extractAccountingData(PCFMessage pcfMsg) throws Exception {
        AccountingData data = new AccountingData();
//        TODO: it says in the constants package documentation that MQIAMO_PUTS is an int
//        yet in my testing case it's a list so need to ask Mark Bluemel
        
        // TODO: get opencount and close count??
        data.setUserIdentifier(pcfMsg.getStringParameterValue(MQConstants.MQCACF_USER_IDENTIFIER).trim());
        data.setAppName(pcfMsg.getStringParameterValue(MQConstants.MQCACF_APPL_NAME).trim());
        data.setStartDate(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_DATE).trim());
        data.setStartTime(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_START_TIME).trim());
        data.setEndDate(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_DATE).trim());
        data.setEndTime(pcfMsg.getStringParameterValue(MQConstants.MQCAMO_END_TIME).trim());
        // variables which aren't always returned follow
        try {
            String connName = pcfMsg.getStringParameterValue(MQConstants.MQCACH_CONNECTION_NAME).trim();
            data.setConnName(connName);
        } catch (Exception e) {
            data.setConnName("");
        }

        
        try {
            int[] putsArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUTS);
            data.setPuts((putsArray != null && putsArray.length > 0) ? putsArray[0] : 0);
        } catch (Exception e) {
            data.setPuts(0);
        }

        try {
            int[] put1sArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_PUT1S);
            data.setPut1s((put1sArray != null && put1sArray.length > 0) ? put1sArray[0] : 0);
        } catch (Exception e) {
            data.setPut1s(0);
        }

        try {
            int[] getsArray = pcfMsg.getIntListParameterValue(MQConstants.MQIAMO_GETS);
            data.setGets((getsArray != null && getsArray.length > 0) ? getsArray[0] : 0);
        } catch (Exception e) {
            data.setGets(0);
        }
        
        
        // getting the integer values
        try {
            Integer putsFailedValue = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUTS_FAILED);
            data.setPutsFailed(putsFailedValue);
        } catch (Exception e) {
            data.setPutsFailed(0);
            System.out.println("Could not get putsfailed data for Accouting Message");
        }
        
        try {
            Integer setPut1sFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_PUT1S_FAILED);
            data.setPut1sFailed(setPut1sFailed);
        } catch (Exception e) {
            data.setPut1sFailed(0);
            System.out.println("Could not get put1sfailed data for Accouting Message");
        }
        
        try {
            Integer setGetsFailed = pcfMsg.getIntParameterValue(MQConstants.MQIAMO_GETS_FAILED);
            data.setGetsFailed(setGetsFailed);
        } catch (Exception e) {
            data.setGetsFailed(0);
            System.out.println("Could not get getsfailed data for Accouting Message");
        }


        return data;
    }
}
