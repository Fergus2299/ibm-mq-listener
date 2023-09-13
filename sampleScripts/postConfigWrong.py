import requests
import json

# URL to the POST endpoint
POST_URL = 'https://localhost:8080/updateConfig'

# Modified payload to cause a type mismatch error for connThreshold
payload = {
    "retrievedThresholds": {
        "apps": {
            "connThreshold": -0.5,  # This will cause a type mismatch error
            "connOpRatioThreshold": 0.8,
            "minimumConns": 2
        },
        "queue_manager": {
            "errorThreshold": -5,
            "maxMQConns": 100,
            "maxMQOps": 50
        },
        "queues": {
            "errorThreshold": 3,
            "queueThresholds": {
                "sampleQueue1": {
                    "activity": 20,
                    "depth": 40
                },
                "sampleQueue2": {
                    "activity": 30,
                    "depth": 40
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
