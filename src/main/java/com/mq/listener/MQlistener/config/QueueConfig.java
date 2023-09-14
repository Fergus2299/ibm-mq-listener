package com.mq.listener.MQlistener.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("config")
public class QueueConfig {
    private List<String> queues = new ArrayList<>();

	public List<String> getQueues() {
		return queues;
	}

	public void setQueues(List<String> queues) {
		this.queues = queues;
	}
	
    @Override
    public String toString() {
        return "Queues being read =" + queues;
    }
}