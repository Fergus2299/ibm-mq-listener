package com.mq.listener.MQlistener.Listeners;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ibm.mq.headers.pcf.PCFMessage;
import com.mq.listener.MQlistener.processors.PerformanceProcessor;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
class PerformanceListenerTest {
    private PerformanceProcessor mockPerformanceProcessor;
    private PerformanceListener performanceListener;

    @BeforeEach
    public void setUp() {
        mockPerformanceProcessor = Mockito.mock(PerformanceProcessor.class);
        performanceListener = new PerformanceListener(mockPerformanceProcessor);
    }

    @Test
    public void testListen_withBytesMessage() throws JMSException {
        // Mock
        BytesMessage mockMessage = Mockito.mock(BytesMessage.class);
        Mockito.when(mockMessage.getBodyLength()).thenReturn(10L); // Example data

        // Action
        performanceListener.listen(mockMessage);

        // Assert
        Mockito.verify(mockPerformanceProcessor, Mockito.times(1)).processPerformanceMessage(Mockito.any(PCFMessage.class));
    }
}