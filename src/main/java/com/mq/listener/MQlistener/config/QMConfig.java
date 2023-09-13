package com.mq.listener.MQlistener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


public class QMConfig {
    private Connections connections = new Connections();
    private Operations operations = new Operations();
    private Errors errors = new Errors();

    // getters and setters for connections, operations, and errors

    public static class Connections {
        private int max;

		public int getMax() {
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

    public static class Operations {
        private int max;

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}
        @Override
        public String toString() {
            return "Operations [max=" + max + "]";
        }
        
    }

    public static class Errors {
        private int max;

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}
        @Override
        public String toString() {
            return "Errors [max=" + max + "]";
        }
        
    }

	public Connections getConnections() {
		return connections;
	}

	public void setConnections(Connections connections) {
		this.connections = connections;
	}

	public Operations getOperations() {
		return operations;
	}

	public void setOperations(Operations operations) {
		this.operations = operations;
	}

	public Errors getErrors() {
		return errors;
	}

	public void setErrors(Errors errors) {
		this.errors = errors;
	}
	
    @Override
    public String toString() {
        return "QueueManagerConfig [connections=" + connections + ", operations=" + operations + ", errors=" + errors + "]";
    }
    
    public void print() {
        System.out.println(this.toString());
    }

    
}
