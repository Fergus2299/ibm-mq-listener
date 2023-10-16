# ibm-mq-listener

This is a component of the MQMerlin application, it can connect to an IBM MQ instance, listens for events, accounting and statistics messages. It then detects, tracks and sends issues within MQ based on use-set thresholds.

Link to MQMerlin: https://github.com/Zaid1120/MQMerlin


## Prerequisites

- **Java**: Supported versions are Java 17 and Java 20.
- **IBM MQ**: Tested with IBM MQ version 9.
- **Maven**: Ensure you have [Maven](https://maven.apache.org/download.cgi) installed.


### Current Supported Issues
- ActivitySpike: If there are too many operations on any one queue or the whole queue manager. Or too many applications try to connect to the queue manager.
- ErrorSpike: If there are too many 2035 or 2085 errors in one particular queue manager then this could be indicative of a cyber-attack. 
- ConnectionPatternIssue: If a specific app connects too many times or is connecting too much for the operations it's performing - such as the case in Figure 1 where both apps are achieving the same task. This analysis is only performed if an app is a 'frequent connector'.
- ServiceIntervalHigh: Maps directly to IBM MQ queue service high event, means that a queue is filling up too much. 
  
![image](https://github.com/Fergus2299/ibm-mq-listener/assets/114816708/1f1224aa-f7e8-431f-8fc8-0040222d9b22)
*Figure 1: Two different applications achieving the same task.*

## Setup & Installation

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Zaid1120/ibm-mq-listener.git
   cd ibm-mq-listener

2. **Configure the Application**
   - Edit 'application.yml' to include the connection details of your MQ instance.
   - See 'Threshold Cofiguration' section below.


4. **Build using Maven**:

   ```bash
   mvn clean install

5. **Run the Application**
   ```bash
   java -jar target/ibm-mq-listener-01.jar

   
### Threshold Configuration

Thresholds can be configured using a GUI when using MQMerlin, however you do have the option to configure the app when just running the backend. Navigate to '/config/config.json'. The JSON file might look like this:

### Structure:

- **qms**: Represents all queue managers.
  
  - **`<QM_NAME>`**: Each queue manager's specific settings.
  
    - **app**: Application-level thresholds.
      - **connections**: Maximum number of allowed connections for an application.
      - **connectionOperationsRatio**: Maximum ratio of operations to connections for an application, and the minimum number of connections for this threshold to apply.
      
    - **queueManager**: Queue Manager-level thresholds.
      - **connections**: Maximum number of allowed connections to this queue manager.
      - **operations**: Maximum number of operations allowed on this queue manager.
      - **errors**: Maximum number of errors allowed on this queue manager.
      
    - **queue**: Queue-level thresholds.
      - **errors**: Maximum number of errors allowed on a specific queue.
      - **operationsDefault**: Default maximum operations allowed on a queue.
      - **operationsSpecificQueues**: Specific operation thresholds for designated queues.
