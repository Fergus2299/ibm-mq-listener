package com.mq.listener.MQlistener.config;

import java.util.Map;

public class ConfigDataTransferObject {
    private RetrievedThresholdsDTO retrievedThresholds;

    public static class RetrievedThresholdsDTO {
        private AppDTO apps;
        private QueueManagerDTO queue_manager;
        private QueueDTO queues;
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
        private Map<String, QueueThresholdDTO> queueThresholds;        
        
		public int getErrorThreshold() {
			return errorThreshold;
		}
		public void setErrorThreshold(int errorThreshold) {
			this.errorThreshold = errorThreshold;
		}
		public Map<String, QueueThresholdDTO> getQueueThresholds() {
			return queueThresholds;
		}
		public void setQueueThresholds(Map<String, QueueThresholdDTO> queueThresholds) {
			this.queueThresholds = queueThresholds;
		}


    }
    
    public static class QueueThresholdDTO {
        private int depth = 0; // As required, defaulting to 0
        private int activity;
		public int getDepth() {
			return depth;
		}
		public void setDepth(int depth) {
			this.depth = depth;
		}
		public int getActivity() {
			return activity;
		}
		public void setActivity(int activity) {
			this.activity = activity;
		}



    }

	public RetrievedThresholdsDTO getRetrievedThresholds() {
		return retrievedThresholds;
	}

	public void setRetrievedThresholds(RetrievedThresholdsDTO retrievedThresholds) {
		this.retrievedThresholds = retrievedThresholds;
	}


    
}
