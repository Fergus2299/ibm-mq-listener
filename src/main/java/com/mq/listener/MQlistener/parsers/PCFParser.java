package com.mq.listener.MQlistener.parsers;

import com.ibm.mq.headers.*;
import com.ibm.mq.headers.pcf.MQCFGR;
import com.ibm.mq.headers.pcf.MQCFH;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFParameter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.ibm.mq.constants.MQConstants;

public class PCFParser {
    private static final Logger log = LoggerFactory.getLogger(PCFParser.class);
    
    
    /**
     * Logs the details of a provided PCFMessage to the console.
     * 
     * This method processes and logs the header information first by invoking the `parsePCFHeader` method.
     * Next, it extracts and logs parameters contained within the PCFMessage. 
     * For parameters of type MQCFGR, the method also logs any nested parameters.
     * If any nested parameter is related to command lookup, its associated 'lookupValue'
     * is logged for clarity.
     * 
     * Note: The actual logging mechanism might be improved in future versions to handle various
     * log levels and to providetoPcfMessageJson more contextual information.
     * 
     * @param pcfMsg The PCFMessage object whose details are to be logged.
     * @throws Nothing yet, as error handling has not been included.
     */
    public static void parsePCFMessage(PCFMessage pcfMsg) {
        if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return;
        }
    	try {
    		parsePCFHeader(pcfMsg);
	        Enumeration<?> parameters = pcfMsg.getParameters();
	        while (parameters.hasMoreElements()) {
	            PCFParameter parameter = (PCFParameter) parameters.nextElement();
	            if (parameter instanceof MQCFGR) {
	                MQCFGR fgr = (MQCFGR) parameter;
	                System.out.format("\t%s : %s%n", fgr.getParameterName(), fgr.getParameterCount());
	                Enumeration<?> nestedParameters = fgr.getParameters();
	                while (nestedParameters.hasMoreElements()) {
	                    PCFParameter nestedParameter = (PCFParameter) nestedParameters.nextElement();
	                    switch (nestedParameter.getParameter()) {
	                        case MQConstants.MQIACF_COMMAND:
	                            System.out.format("\t\t%s : %s%n", nestedParameter.getParameterName(), MQConstants.lookup(nestedParameter.getValue(), "MQCMD_.*"));
	                            break;
	                        case MQConstants.MQIACF_EVENT_ORIGIN:
	                            System.out.format("\t\t%s : %s%n", nestedParameter.getParameterName(), MQConstants.lookup(nestedParameter.getValue(), "MQEVO_.*"));
	                            break;
	                        case MQConstants.MQIA_Q_TYPE:
	                            System.out.format("\t\t%s : %s%n", nestedParameter.getParameterName(), MQConstants.lookup(nestedParameter.getValue(), "MQQT_.*"));
	                            break;
	                        default:
	                            System.out.format("\t\t%s : %s%n", nestedParameter.getParameterName(), nestedParameter.getStringValue());
	                    }
	                }
	            } else {
	            	String paramName = trimString(parameter.getParameterName());
	                String paramValue = trimString(parameter.getStringValue());
	                System.out.format("\t%s : %s%n", paramName, paramValue);
	            }
	        }
    	} catch (RuntimeException e) {
            System.out.println("Error occurred while parsing PCFMessage: " + e.getMessage());
            e.printStackTrace();
            log.error("Error occurred while parsing PCFMessage: " + e.getMessage(), e);
        }
	        
    }
	
	/**
	 * Logs the header details of a provided PCFMessage.
	 * 
	 * The method extracts various attributes from the PCF message's header, including:
	 * - type: Which is expected to return 7 for an MQCFT_EVENT.
	 * - command: Important for understanding the type of event (e.g., MQCMD_Q_MGR_EVENT).
	 * - completion code.
	 * - reason: Gives more context about the event (e.g., MQRC_NOT_AUTHORIZED signifies a 2035 error).
	 * - parameter count.
	 * 
	 * The extracted attributes are then logged using the system's logger with informative 
	 * context. Where applicable, the method uses the MQConstants lookup to log human-readable 
	 * string representations for the command and reason.
	 * 
	 * @param pcfMsg The PCFMessage object whose header details are to be logged.
	 */
    public static void parsePCFHeader(PCFMessage pcfMsg) {
    	
        if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return;
        }
    	try {
    		MQCFH cfh = pcfMsg.getHeader();

	        // type should return 7 in this case because this corresponds to an MQCFT_EVENT
	        int type = cfh.getType();
	        // this is important to our use case because it tells us what kind of event (e.g., MQCMD_Q_MGR_EVENT)
	        int command = cfh.getCommand();
	        int compCode = cfh.getCompCode();
	        // this is important to our use case because it gives more information about this event
	        // e.g., MQRC_NOT_AUTHORIZED tells us that it's a 2035 error
	        int reason = cfh.getReason();
	        int paramCount = cfh.getParameterCount();
	
	        System.out.println("PCF Header Details:");
	        System.out.format("Type: %d%n", type);
	        System.out.format("Command: %d (%s)%n", command, MQConstants.lookup(command, "MQCMD_.*"));
	        System.out.format("Completion Code: %d%n", compCode);
	        System.out.format("Reason Code: %d (%s)%n", reason, MQConstants.lookupReasonCode(reason));
	        System.out.format("Parameter Count: %d%n", paramCount);
    	} catch (RuntimeException e) {
            System.out.println("Error occurred while parsing PCFHeader: " + e.getMessage());
            e.printStackTrace();
            log.error("Error occurred while parsing PCFHeader: " + e.getMessage(), e);
        }
        
    }
    
    /**
     * Converts a given PCFMessage into a JSON string representation.
     * 
     * The generated JSON is divided into two primary sections:
     * 1. `header`: Contains details extracted from the PCF message's header, 
     *    like type, command, completion code, reason, and parameter count. 
     *    The detailed header information is sourced from the `toJsonPcfHeaders` method.
     * 
     * 2. `parameters`: Represents a collection of parameters from the PCFMessage. 
     *    For parameters of type MQCFGR, nested parameters are also included. 
     *    Any nested parameter related to command lookup will have an associated 'lookupValue'
     *    for a clearer representation.
     * 
     * @param pcfMsg The PCFMessage object to be converted into JSON.
     * @return A string representation of the PCFMessage in JSON format, 
     *         with an indentation of 4 spaces for readability.
     * @throws MQDataException if an error occurs during data extraction from the PCFMessage.
     */
    public static String toPcfMessageJson(PCFMessage pcfMsg) throws MQDataException {
        if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return "{}"; // return empty JSON string
        }
        JSONObject jsonObject = new JSONObject();
        try {
	        // Add the header to the JSON object
	        jsonObject.put("header", new JSONObject(toJsonPcfHeaders(pcfMsg)));
	
	        // Process parameters
	        JSONArray paramsArray = new JSONArray();
	        Enumeration<?> parameters = pcfMsg.getParameters();
	        while (parameters.hasMoreElements()) {
	            PCFParameter parameter = (PCFParameter) parameters.nextElement();
	            JSONObject paramObject = new JSONObject();
	
	            if (parameter instanceof MQCFGR) {
	                MQCFGR fgr = (MQCFGR) parameter;
	                paramObject.put("parameterName", fgr.getParameterName());
	                paramObject.put("parameterCount", fgr.getParameterCount());
	                JSONArray nestedParamsArray = new JSONArray();
	                Enumeration<?> nestedParameters = fgr.getParameters();
	                while (nestedParameters.hasMoreElements()) {
	                    PCFParameter nestedParameter = (PCFParameter) nestedParameters.nextElement();
	                    JSONObject nestedParamObject = new JSONObject();
	                    nestedParamObject.put("parameterName", nestedParameter.getParameterName());
	
	                    switch (nestedParameter.getParameter()) {
	                        case MQConstants.MQIACF_COMMAND:
	                            nestedParamObject.put("parameterValue", MQConstants.lookup(nestedParameter.getValue(), "MQCMD_.*"));
	                            break;
	                        case MQConstants.MQIACF_EVENT_ORIGIN:
	                            nestedParamObject.put("parameterValue", MQConstants.lookup(nestedParameter.getValue(), "MQEVO_.*"));
	                            break;
	                        case MQConstants.MQIA_Q_TYPE:
	                            nestedParamObject.put("parameterValue", MQConstants.lookup(nestedParameter.getValue(), "MQQT_.*"));
	                            break;
	                        default:
	                            nestedParamObject.put("parameterValue", nestedParameter.getStringValue());
	                    }
	                    nestedParamsArray.put(nestedParamObject);
	                }
	                paramObject.put("nestedParameters", nestedParamsArray);
	            } else {
	            	String paramName = trimString(parameter.getParameterName());
	                String paramValue = trimString(parameter.getStringValue());
	                paramObject.put("parameterName", paramName);
	                paramObject.put("parameterValue", paramValue);
	                
	                // old logic where the strings were'nt cleaned up
//	                paramObject.put("parameterName", parameter.getParameterName());
//	                paramObject.put("parameterValue", parameter.getStringValue());
	            }
	
	            paramsArray.put(paramObject);
	        }
	
	        jsonObject.put("parameters", paramsArray);
        } catch (Exception e) {
            System.out.println("Error occurred while converting PCF headers to JSON: " + e.getMessage());
            e.printStackTrace();
            log.error("Error occurred while converting PCFMessage to JSON: " + e.getMessage(), e);
            return "{}";
        }



        return jsonObject.toString(4);  // Indentation of 4 spaces for prettified JSON
    }
    
    /**
     * Converts the header details of a provided PCFMessage into a JSON format.
     * 
     * This method extracts various attributes from the message's header, including:
     * - type
     * - command (and its human-readable string representation)
     * - completion code
     * - reason (and its human-readable string representation)
     * - parameter count
     * 
     * The command and reason attributes have additional string representations 
     * (`commandString` and `reasonString`) for clearer understanding, 
     * derived using the MQConstants lookup.
     * 
     * @param pcfMsg The PCFMessage object from which header details are to be extracted.
     * @return A string representation of the extracted header details in JSON format.
     * @throws MQDataException if an error occurs during header data extraction from the PCFMessage.
     */
    public static String toJsonPcfHeaders(PCFMessage pcfMsg) {
        if (pcfMsg == null) {
            log.error("Provided PCFMessage is null.");
            return "{}"; // return empty JSON string
        }
        JSONObject jsonObject = new JSONObject();
        
        try {
        	// getting the header information
	        MQCFH cfh = pcfMsg.getHeader();
	        // get all the params within the header
	        int type = cfh.getType();
	        int command = cfh.getCommand();
	        int compCode = cfh.getCompCode();
	        int reason = cfh.getReason();
	        int paramCount = cfh.getParameterCount();
	
	        // Adding the lookup values for command and reason to the JSON object
	        jsonObject.put("type", type);
	        jsonObject.put("command", command);
	        jsonObject.put("commandString", MQConstants.lookup(command, "MQCMD_.*"));
	        jsonObject.put("compCode", compCode);
	        jsonObject.put("reason", reason);
	        jsonObject.put("reasonString", MQConstants.lookupReasonCode(reason));
	        jsonObject.put("paramCount", paramCount);
	        
        } catch (Exception e) {
            System.out.println("Error occurred while converting PCF headers to JSON: " + e.getMessage());
            e.printStackTrace();
            log.error("Error occurred while converting PCF headers to JSON: " + e.getMessage(), e);
            return "{}";
        }
        
        return jsonObject.toString();
    }
    
    
    public static void saveJsonToFile(String jsonString, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            file.write(jsonString);
            log.info("Successfully wrote JSON data to {}", fileName);
        } catch (IOException e) {
            log.error("Error occurred while writing JSON to file", e);
        }
    }
    
    /**
     * Cleans up a provided string by trimming unnecessary whitespace and other unwanted characters.
     * 
     * This method ensures that outputted strings, especially from external sources or automated systems, 
     * are presented in a readable and consistent format. The need for such a method was identified 
     * after observing that some of the strings in the generated JSON, particularly names and identifiers, 
     * had excessive trailing spaces that made the output look untidy and could potentially cause issues 
     * in downstream systems or applications. By using this method, we can guarantee that any string 
     * embedded in our output is neat and free of unnecessary padding or characters, thereby improving 
     * readability and ensuring compatibility with consumers of this data.
     *
     * @param input The original string that might contain unnecessary whitespace or unwanted characters.
     * @return A cleaned-up version of the input string, with whitespace trimmed and unwanted characters removed.
     */
    public static String trimString(String input) {
        return input == null ? null : input.trim();
    }  
}