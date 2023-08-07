package com.mq.listener.MQlistener;

import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.constants.MQConstants;
import org.json.JSONObject;


public class PcfJsonParser {

    public static String toPcfJson(PCFMessage pcfMsg) throws MQDataException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("Type", pcfMsg.getType());
//        jsonObject.put("StrucLength", pcfMsg.getStrucLength());
//        jsonObject.put("Version", pcfMsg.getVersion());
        jsonObject.put("Command", pcfMsg.getCommand());
        jsonObject.put("MsgSeqNumber", pcfMsg.getMsgSeqNumber());
        jsonObject.put("Control", pcfMsg.getControl());
        jsonObject.put("CompCode", pcfMsg.getCompCode());
        jsonObject.put("Reason", pcfMsg.getReason());
        jsonObject.put("ParameterCount", pcfMsg.getParameterCount());
        if (pcfMsg.getParameterValue(2015) != null) {
            jsonObject.put("QMgrName", (String) pcfMsg.getParameterValue(2015));
        }
        if (pcfMsg.getParameterValue(2016) != null) {
            jsonObject.put("QName", (String) pcfMsg.getParameterValue(2016));
        }
        if (pcfMsg.getParameterValue(3025) != null) {
            jsonObject.put("UserId", (String) pcfMsg.getParameterValue(3025));
        }
        if (pcfMsg.getParameterValue(3024) != null) {
            jsonObject.put("AppName", (String) pcfMsg.getParameterValue(3024));
        }

        return jsonObject.toString();
    }
}