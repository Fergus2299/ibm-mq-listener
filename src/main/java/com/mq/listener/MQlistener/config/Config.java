package com.mq.listener.MQlistener.config;

import java.util.Map;

import com.mq.listener.MQlistener.config.ConfigDataTransferObject.*;


public class Config {
	// TODO: error handling for updateFromDTO methods, check for empty Dto's etc
    private Map<String, QMConfig> qms;
    private static Integer OPERATIONS_DEFAULT = 1000;

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
	            @Override
	            public String toString() {
	                return "ConnectionConfig [max=" + max + "]";
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
	            @Override
	            public String toString() {
	                return "ConnectionOperationsRatioConfig [max=" + max + ", connections=" + connections + "]";
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
			// sets new values of this class from a DTO
			public void updateFromDTO(AppDTO dTO) {
				this.connections.setMax(dTO.getConnThreshold());
				this.connectionOperationsRatio.setConnections(dTO.getMinimumConns());
				this.connectionOperationsRatio.setMax(dTO.getConnOpRatioThreshold());
			}
			@Override
	        public String toString() {
	            return "AppConfig [connections=" + connections + ", connectionOperationsRatio=" + connectionOperationsRatio + "]";
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
				
	            @Override
	            public String toString() {
	                return "ConnectionConfig [max=" + max + "]";
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
	            @Override
	            public String toString() {
	                return "OperationsConfig [max=" + max + "]";
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
		           @Override
		            public String toString() {
		                return "ErrorsConfig [max=" + max + "]";
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
			
			// sets new values of this class from a DTO
			public void updateFromDTO(QueueManagerDTO dTO) {
				this.connections.setMax(dTO.getMaxMQConns());
				this.operations.setMax(dTO.getMaxMQOps());
				this.errors.setMax(dTO.getErrorThreshold());
			}
			@Override
	        public String toString() {
	            return "QueueManagerConfig [connections=" + connections + ", operations=" + operations + ", errors=" + errors + "]";
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
	            @Override
	            public String toString() {
	                return "ErrorsConfig [max=" + max + "]";
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
			
			// sets new values of this class from a DTO
			// TODO: for now not changing operations default, this is a possible future extension
			public void updateFromDTO(QueueDTO dTO) {
				this.errors.setMax(dTO.getErrorThreshold());
				// for now the default value is used
				this.setOperationsDefault(OPERATIONS_DEFAULT);
				 for (Map.Entry<String, QueueThresholdDTO> entry : dTO.getQueueThresholds().entrySet()) {
			            String queueName = entry.getKey();
			            QueueThresholdDTO queueThreshold = entry.getValue();
			            // only updating stored activity value
			            this.operationsSpecificQueues.put(queueName, queueThreshold.getActivity());
			        }
				
			}
	        @Override
	        public String toString() {
	            return "QueueConfig [errors=" + errors + ", operationsDefault=" + operationsDefault + ", operationsSpecificQueues=" + operationsSpecificQueues + "]";
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
        
		
	    @Override
	    public String toString() {
	        return "QMConfig [app=" + app + ", queueManager=" + queueManager + ", queue=" + queue + "]";
	    }
    }

	public Map<String, QMConfig> getQms() {
		return qms;
	}

	public void setQms(Map<String, QMConfig> qms) {
		this.qms = qms;
	}
	
	// a printing statement for this class
	@Override
	public String toString() {
	    StringBuilder sb = new StringBuilder();
	    sb.append("Config [");
	    if (qms != null) {
	        for (Map.Entry<String, QMConfig> entry : qms.entrySet()) {
	            sb.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append(", ");
	        }
	    }
	    sb.append("]");
	    return sb.toString();
	}
    
}