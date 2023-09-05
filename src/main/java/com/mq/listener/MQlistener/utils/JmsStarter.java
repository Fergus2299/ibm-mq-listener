package com.mq.listener.MQlistener.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jms.config.JmsListenerEndpointRegistry;
import org.springframework.stereotype.Component;


/**
 * JmsStarter is a component responsible for manually starting JMS listeners during 
 * the application's startup sequence. This is crucial when we want to delay the 
 * initialization of these listeners to after certain operations, like clearing MQ queues.
 * To utilize this behavior, ensure that 'spring.jms.listener.auto-startup' is set to 'false'
 * in the application properties.
 */
@Component
@Order(2)
public class JmsStarter implements ApplicationRunner {

    @Autowired
    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;

    @Override
    public void run(ApplicationArguments args) {
        jmsListenerEndpointRegistry.start();
    }
}