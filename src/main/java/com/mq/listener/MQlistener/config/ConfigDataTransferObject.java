package com.mq.listener.MQlistener.config;

import java.util.Map;

public class ConfigDataTransferObject {
    private AppDTO apps;
    private QueueManagerDTO queue_manager;
    private QueueDTO queues;
    
    public static class AppDTO {
        private int connThreshold;
        private float connOpRatioThreshold;
        private int minimumConns;
		public int getConnThreshold() {
			return connThreshold;
		}
		public void setConnThreshold(int connThreshold) {
			this.connThreshold = connThreshold;
		}
		public float getConnOpRatioThreshold() {
			return connOpRatioThreshold;
		}
		public void setConnOpRatioThreshold(float connOpRatioThreshold) {
			this.connOpRatioThreshold = connOpRatioThreshold;
		}
		public int getMinimumConns() {
			return minimumConns;
		}
		public void setMinimumConns(int minimumConns) {
			this.minimumConns = minimumConns;
		}

    }

    public static class QueueManagerDTO {
        private int errorThreshold;
        private int maxMQConns;
        private int maxMQOps;
		public int getErrorThreshold() {
			return errorThreshold;
		}
		public void setErrorThreshold(int errorThreshold) {
			this.errorThreshold = errorThreshold;
		}
		public int getMaxMQConns() {
			return maxMQConns;
		}
		public void setMaxMQConns(int maxMQConns) {
			this.maxMQConns = maxMQConns;
		}
		public int getMaxMQOps() {
			return maxMQOps;
		}
		public void setMaxMQOps(int maxMQOps) {
			this.maxMQOps = maxMQOps;
		}

    }

    public static class QueueDTO {
        private int errorThreshold;
        private Map<String, Integer> queueActivityThresholds;
		public int getErrorThreshold() {
			return errorThreshold;
		}
		public void setErrorThreshold(int errorThreshold) {
			this.errorThreshold = errorThreshold;
		}
		public Map<String, Integer> getQueueActivityThresholds() {
			return queueActivityThresholds;
		}
		public void setQueueActivityThresholds(Map<String, Integer> queueActivityThresholds) {
			this.queueActivityThresholds = queueActivityThresholds;
		}

    }

	public AppDTO getApps() {
		return apps;
	}

	public void setApps(AppDTO apps) {
		this.apps = apps;
	}

	public QueueManagerDTO getQueue_manager() {
		return queue_manager;
	}

	public void setQueue_manager(QueueManagerDTO queue_manager) {
		this.queue_manager = queue_manager;
	}

	public QueueDTO getQueues() {
		return queues;
	}

	public void setQueues(QueueDTO queues) {
		this.queues = queues;
	}
    
    
}
