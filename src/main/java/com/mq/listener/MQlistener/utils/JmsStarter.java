//package com.mq.listener.MQlistener.utils;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.jms.config.JmsListenerEndpointRegistry;
//import org.springframework.jms.listener.AbstractMessageListenerContainer;
//import org.springframework.jms.listener.MessageListenerContainer;
//import org.springframework.stereotype.Component;
//
//
///**
// * JmsStarter manually starts JMS listeners during 
// * the application's startup sequence. It waits until after queues
// * have been cleared.
// */
//@Component
//@Order(3)
//public class JmsStarter implements ApplicationRunner {
//	
//	
//
//    @Autowired
//    private JmsListenerEndpointRegistry jmsListenerEndpointRegistry;
//	@Autowired
//	private SendLoginStatus sendLoginStatus;
//	
//    @Override
//    public void run(ApplicationArguments args) {
//        jmsListenerEndpointRegistry.start();
//        
//        // this section counts which of the 
//        int count = 0;
//        for (MessageListenerContainer container : jmsListenerEndpointRegistry.getListenerContainers()) {
//            if (container instanceof AbstractMessageListenerContainer) {
//                AbstractMessageListenerContainer abstractContainer = (AbstractMessageListenerContainer) container;
//                if (abstractContainer.isRunning()) {
//                    System.out.println("Listener for " + abstractContainer.getDestinationName() + " is running.");
//                    count ++;
//                }
//            }
//        }
//        if (count >= 4) {
//            sendLoginStatus.sendStatus(true, "Login successful");
//        } else {
//            String errorMessage = "Login unsuccessful: NOTE TO SELF: make better message later on";
//            System.out.println(errorMessage.toString());
//            sendLoginStatus.sendStatus(false, errorMessage.toString());
//        }
//    }
//}