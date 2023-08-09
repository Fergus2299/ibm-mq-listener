package com.mq.listener.MQlistener;


import com.ibm.mq.headers.pcf.*;

public class PCFExtractor {

    public void extractAndLog(PCFMessage pcfMessage) {
        List<PCFParameter> parameters = pcfMessage.getParameters();

        for (PCFParameter parameter : parameters) {
            String constantName = parameter.getParameterName();
            String value = extractValue(parameter);
            
            if (constantName != null && value != null) {
                String logMessage = constantName + " : " + value.trim();
                System.out.println(logMessage); // Replace with your logging mechanism
            }
        }
    }

    private String extractValue(PCFParameter parameter) {
        if (parameter instanceof MQCFST) {
            return ((MQCFST) parameter).getStringValue();
        } else if (parameter instanceof MQCFIN) {
            return String.valueOf(((MQCFIN) parameter).getValue());
        } 
        // Extend this for other parameter types as needed
        return null;
    }

    public static void main(String[] args) {
        PCFExtractor extractor = new PCFExtractor();
        extractor.extractAndLog(extractor.sampleMessage);
    }
}