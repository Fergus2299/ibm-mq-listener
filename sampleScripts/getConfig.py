import requests

# URL to the GET endpoint
GET_URL = 'https://localhost:8080/configurations'

response = requests.get(GET_URL, verify=False)
if response.status_code == 200:
    print("Configuration retrieved successfully!")
    print(response.json())
else:
    print(f"Failed to retrieve configuration! HTTP Status Code: {response.status_code}")
    print(response.text)
