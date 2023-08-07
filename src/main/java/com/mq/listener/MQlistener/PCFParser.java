package com.mq.listener.MQlistener;

import com.ibm.mq.headers.*;
import com.ibm.mq.headers.internal.store.ByteStore;
import com.ibm.mq.headers.pcf.PCFException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.MQException;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.CMQCFC;

public class PCFParser {
	public static String parsePCFMessage(PCFMessage pcfMessage) {
	    StringBuilder result = new StringBuilder();
	    result.append("Queue Manager Name: ").append(safelyGetString(pcfMessage, CMQCFC.MQCACF_OBJECT_Q_MGR_NAME)).append("\n");
	    result.append("Queue Name: ").append(safelyGetString(pcfMessage, CMQCFC.MQCACF_TO_Q_NAME)).append("\n");
	    result.append("Reason Code: ").append(safelyGetInt(pcfMessage, CMQCFC.MQIACF_REASON_QUALIFIER)).append("\n");
	    result.append("User ID: ").append(safelyGetString(pcfMessage, CMQCFC.MQCACF_USER_IDENTIFIER)).append("\n");
	    result.append("Application Name: ").append(safelyGetString(pcfMessage, CMQCFC.MQCACF_APPL_NAME)).append("\n");
	    return result.toString();
	}
	private static String safelyGetString(PCFMessage pcfMessage, int parameter) {
	    try {
	        return pcfMessage.getStringParameterValue(parameter);
	    } catch (PCFException e) {
	        return "Not Available";
	    }
	}
	private static int safelyGetInt(PCFMessage pcfMessage, int parameter) {
	    try {
	        return pcfMessage.getIntParameterValue(parameter);
	    } catch (PCFException e) {
	        return -1; // Or some other default value
	    }
	}
}