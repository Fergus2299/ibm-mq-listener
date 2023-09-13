package com.mq.listener.MQlistener.newConfig;

import java.util.Map;

public class Config {

    private Map<String, QMConfig> qms;

    public static class QMConfig {

        private AppConfig app;
        private QueueManagerConfig queueManager;
        private QueueConfig queue;

        public static class AppConfig {

            private ConnectionConfig connections;
            private ConnectionOperationsRatioConfig connectionOperationsRatio;

            public static class ConnectionConfig {
                private int max;

				public int getMax() {
					return max;
				}

				public void setMax(int max) {
					this.max = max;
				}
                
            }

            public static class ConnectionOperationsRatioConfig {
                private double max;
                private int connections;
				public double getMax() {
					return max;
				}
				public void setMax(double max) {
					this.max = max;
				}
				public int getConnections() {
					return connections;
				}
				public void setConnections(int connections) {
					this.connections = connections;
				}
                
                
            }

			public ConnectionConfig getConnections() {
				return connections;
			}

			public void setConnections(ConnectionConfig connections) {
				this.connections = connections;
			}

			public ConnectionOperationsRatioConfig getConnectionOperationsRatio() {
				return connectionOperationsRatio;
			}

			public void setConnectionOperationsRatio(ConnectionOperationsRatioConfig connectionOperationsRatio) {
				this.connectionOperationsRatio = connectionOperationsRatio;
			}
            

        }

        public static class QueueManagerConfig {

            private ConnectionConfig connections;
            private OperationsConfig operations;
            private ErrorsConfig errors;

            public static class ConnectionConfig {
                private int max;

				public int getMax() {
					return max;
				}
				public void setMax(int max) {
					this.max = max;
				}
            }

            public static class OperationsConfig {
                private int max;

				public int getMax() {
					return max;
				}

				public void setMax(int max) {
					this.max = max;
				}
                
            }

            public static class ErrorsConfig {
                private int max;

				public int getMax() {
					return max;
				}

				public void setMax(int max) {
					this.max = max;
				}
                
            }

			public ConnectionConfig getConnections() {
				return connections;
			}

			public void setConnections(ConnectionConfig connections) {
				this.connections = connections;
			}

			public OperationsConfig getOperations() {
				return operations;
			}

			public void setOperations(OperationsConfig operations) {
				this.operations = operations;
			}

			public ErrorsConfig getErrors() {
				return errors;
			}

			public void setErrors(ErrorsConfig errors) {
				this.errors = errors;
			}
            

        }

        public static class QueueConfig {

            private ErrorsConfig errors;
            private int operationsDefault;
            private Map<String, Integer> operationsSpecificQueues;

            public static class ErrorsConfig {
                private int max;

				public int getMax() {
					return max;
				}

				public void setMax(int max) {
					this.max = max;
				}
                
            }

			public ErrorsConfig getErrors() {    			

				return errors;
			}

			public void setErrors(ErrorsConfig errors) {
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

		public AppConfig getApp() {
			return app;
		}

		public void setApp(AppConfig app) {
			this.app = app;
		}

		public QueueManagerConfig getQueueManager() {
			return queueManager;
		}

		public void setQueueManager(QueueManagerConfig queueManager) {
			this.queueManager = queueManager;
		}

		public QueueConfig getQueue() {
			return queue;
		}

		public void setQueue(QueueConfig queue) {
			this.queue = queue;
		}
        
        
    }

	public Map<String, QMConfig> getQms() {
		return qms;
	}

	public void setQms(Map<String, QMConfig> qms) {
		this.qms = qms;
	}
    
}