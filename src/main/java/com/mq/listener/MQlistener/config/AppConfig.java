package com.mq.listener.MQlistener.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config.app")
public class AppConfig {
    private Connections connections = new Connections();
    private Map<String, Object> connectionOperationsRatio;
    
    public Connections getConnections() {
        return connections;
    }
    
    public void setConnections(Connections connections) {
        this.connections = connections;
    }
    
    public Map<String, Object> getConnectionOperationsRatio() {
        return connectionOperationsRatio;
    }
    public void setConnectionOperationsRatio(Map<String, Object> connectionOperations) {
        this.connectionOperationsRatio = connectionOperations;
    }

    
    public static class Connections {
        private int max;

		public int getMax() {
//			System.out.println("max: " + max);
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}
        @Override
        public String toString() {
            return "Connections [max=" + max + "]";
        }
    }
    
    @Override
    public String toString() {
        return "AppConfig [connections=" + connections + ", connectionOperationsRatio=" + connectionOperationsRatio + "]";
    }
    public void print() {
        System.out.println(this.toString());
    }
}
