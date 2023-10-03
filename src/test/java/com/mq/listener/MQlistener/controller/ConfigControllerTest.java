package com.mq.listener.MQlistener.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.mq.listener.MQlistener.config.Config;
import com.mq.listener.MQlistener.config.ConfigDataTransferObject;
import com.mq.listener.MQlistener.config.ConfigManager;
import com.mq.listener.MQlistener.testConfig.TestConfig;

import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(ConfigController.class)
class ConfigControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @SpyBean
    private ConfigManager configManager;


    // test that we can post to this endpoint and status 200
    @Test
    public void getConfigurationsTest() throws Exception {
    	Config mockConfig = TestConfig.createSampleConfig1();
    	when(configManager.getConfig()).thenReturn(mockConfig);
        mockMvc.perform(get("/configurations"))
            .andExpect(status().isOk());
    }
    // test that we can post to this endpoint and status 200
    @Test
    public void updateConfigurationsTest() throws Exception {
    	ConfigDataTransferObject mockConfigDTO = TestConfig.createSampleConfig2().toConfigDataTransferObject("QM1");
        // Mocking configManager's behavior
        doNothing().when(configManager).updateConfigurations(any(ConfigDataTransferObject.class));

        // Send POST request
        mockMvc.perform(post("/updateConfig")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(mockConfigDTO)))
            .andExpect(status().isOk())
            .andExpect(content().string("Configuration updated successfully!"));

        
    }
    	
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
