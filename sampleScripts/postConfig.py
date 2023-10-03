import requests
import json

# URL to the POST endpoint
POST_URL = 'https://localhost:8080/updateConfig'

# Sample payload for the POST request, based on the structure of ConfigDataTransferObject
# Modify this sample payload to fit your actual configuration
payload = {
    "retrievedThresholds": {
        "apps": {
            "connThreshold": 707,
            "connOpRatioThreshold": 0.48,
            "minimumConns": 707
        },
        "queue_manager": {
            "errorThreshold": 6,
            "maxMQConns": 101,
            "maxMQOps": 51
        },
        "queues": {
            "errorThreshold": 26,
            "queueThresholds": {
                "DEV.QUEUE.1": {
                    "activity": 707,
                    "depth":26
                },
                "DEV.QUEUE.2": {
                    "activity": 707,
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
