import requests
import json

# URL to the POST endpoint
POST_URL = 'https://localhost:8080/updateConfig'

# Sample payload for the POST request, based on the structure of ConfigDataTransferObject
# Modify this sample payload to fit your actual configuration
payload = {
    "retrievedThresholds": {
        "apps": {
            "connThreshold": 10,
            "connOpRatioThreshold": 0.8,
            "minimumConns": 2
        },
        "queue_manager": {
            "errorThreshold": 5,
            "maxMQConns": 100,
            "maxMQOps": 50
        },
        "queues": {
            "errorThreshold": 3,
            "queueThresholds": {
                "sampleQueue1": {
                    "activity": 20,
                    "depth":40
                },
                "sampleQueue2": {
                    "activity": 30,
                    "depth":40
                }
            }
        }
    }
}

headers = {
    'Content-Type': 'application/json'
}

response = requests.post(POST_URL, data=json.dumps(payload), headers=headers, verify=False)
if response.status_code == 200:
    print("Configuration updated successfully!")
    print(response.text)
else:
    print(f"Failed to update configuration! HTTP Status Code: {response.status_code}")
    print(response.text)
