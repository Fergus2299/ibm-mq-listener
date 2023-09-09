import requests
import json

# Endpoint URL
url = "https://127.0.0.1:8080/updateAppConfig"

# Sample data based on the Java DTO (Data Transfer Object) structure
data = {
    "apps": {
        "connThreshold": 100,  # Example value
        "connOpRatioThreshold": 0.5,  # Example value
        "minimumConns": 50  # Example value
    },
    "queue_manager": {
        "errorThreshold": 10,  # Example value
        "maxMQConns": 200,  # Example value
        "maxMQOps": 1000  # Example value
    },
    "queues": {
        "errorThreshold": 5,  # Example value
        "queueActivityThresholds": {
            "queue1": 10,  # Example values
            "queue2": 20
        },
            "queueDepthThresholds": {
            "queue1": 10,  # Example values
            "queue2": 20
        }
    }
}

# Post the data
response = requests.post(url, data=json.dumps(data), headers={"Content-Type": "application/json"}, verify=False)


# Print the response
print(response.status_code)
print(response.text)
