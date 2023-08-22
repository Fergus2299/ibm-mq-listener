package com.mq.listener.MQlistener.utils;


import java.io.BufferedWriter;
import java.io.FileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtil {
    private static ObjectMapper mapper = new ObjectMapper();

    public static String toJson(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }
    public static void writeToJsonFile(Object obj, String filePath) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(toJson(obj));
            writer.newLine();
        }
    }
}