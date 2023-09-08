package com.mq.listener.MQlistener.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config.queue")
public class QueueConfig {
	
	private Errors errors = new Errors();
    private int operationsDefault;
    // TODO: insure event when empty this works
	private Map<String, Integer> operationsSpecificQueues;
    
    public static class Errors {
        private int max;
        
		public int getMax() {
//			System.out.println("max: " + max);
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}
        
    }
    
	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}

	public int getOperationsDefault() {
		return operationsDefault;
	}

	public void setOperationsDefault(int operationsDefault) {
		this.operationsDefault = operationsDefault;
	}		
	
	public Map<String, Integer> getOperationsSpecificQueues() {
		return operationsSpecificQueues;
	}

	public void setOperationsSpecificQueues(Map<String, Integer> operationsSpecificQueues) {
		this.operationsSpecificQueues = operationsSpecificQueues;
	}
}