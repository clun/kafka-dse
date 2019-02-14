# Kafka Dse Sink Sample

From version 6.7, DataStax provides a `sink` to allow reading messages from Kafka and inserting data into DSE. 
In the following project we would like to demonstration this capability.

**Projects overview :**
- [kafka-dse-conf](kafka-dse-conf) folder with my dse-sink configuration file (Mapping)
- [kafka-dse-core](kafka-dse-core) is a library holding the domain beans and utility classes
- [kafka-dse-producer](kafka-dse-producer) is a standalone Spring Boot application that generates random Tick Data from static CSV 
and push them into multiple topics (for multiple formats) String, Avro, Json, Struct
- [kafka-dse-webui](kafka-dse-webui) is a standalone Spring Boot application provide a UI.

<img src="./pics/02-archi.png" height="400" />

--- 

**Table Of Content**

- [Installation](#setup-your-station)
- [Start the Producer](#start-the-producer)
- [Start Web Application](#start-web-application)


## Setup your station

### INSTALLATION

 * **Install Zookeeper and Kafka**

```bash
brew install kafka
```

 * **Install Kafka Registry**

```bash 
git clone https://github.com/confluentinc/common.git
cd common; git checkout v4.1.1; mvn install -DskipTests
cd ..
git clone https://github.com/confluentinc/rest-utils.git
cd rest-utils; git checkout v4.1.1; mvn install -DskipTests
cd ..
git clone https://github.com/confluentinc/schema-registry.git
cd schema-registry; git checkout v4.1.1; mvn install -DskipTests
cd..
```

 * **Install DSE Sink for KAFKA CONNECT**

```bash
Extract zip to find kafka-connect-dse-1.0.0-alpha1.jar
Edit file : connect-standalone.properties to point to the JAR
plugin.path=/Users/cedricklunven/apps/kafka/kafka-connect-dse-1.0.0-alpha1/kafka-connect-dse-1.0.0-alpha1.jar

Copy file dse-sink-standalone.properties.sample in kafka directory and rename to dse-sink
Config dse-sink.properties with contactPoint and information about DSE
```

### START SERVERS

```bash
# Start DSE
dse cassandra -s -g -k

# Start zookeeper
zookeeper-server-start /usr/local/etc/kafka/zookeeper.properties &> /Users/cedricklunven/apps/kafka/kafka_zookeeper.log &

# Start Kafka
kafka-server-start /usr/local/etc/kafka/server.properties &> /Users/cedricklunven/apps/kafka/kafka-server.log &

# Start Registry
schema-registry/bin/schema-registry-start schema-registry/config/schema-registry.properties &> /Users/cedricklunven/apps/kafka/kafka_schema_registry.log &

# Start connector
connect-standalone /usr/local/etc/kafka/connect-standalone.properties /usr/local/etc/kafka/dse-sink.properties &> /Users/cedricklunven/apps/kafka/kafka_connect.log &
```

### CHECK STATUS

```bash
# Check Zookeeper
telnet localhost 2181

# Check DSE
dsetool status

# Check kafka SERVER
telnet localhost 9092

# Check kafka REGISTRY
curl http://localhost:8081

# Check kafka CONNECT 9with DSE-SINK)
curl http://localhost:8083
```

### WORK WITH TOPICS AND CONSUMERS

```bash
# List Kafka Topics
kafka-topics --list --zookeeper localhost:2181

# Create a topic
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tick-stream
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tick-stream-avro
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tick-stream-json
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tick-stream-struct

# Consume STRING 
kafka-console-consumer --bootstrap-server localhost:2181 --topic tick-stream --from-beginning

# Consume AVRO
schema-registry/bin/kafka-avro-console-consumer --topic tick-stream-avro --zookeeper localhost:2181 --from-beginning --property print.key=true

# List Kafka Consumer Group
kafka-consumer-groups --list --bootstrap-server localhost:9092

# Infor on a consumer group
kafka-consumer-groups --bootstrap-server localhost:9092 --group xxx --describe
```

### STOP SERVERS

```bash
# Stop Dse
dse cassandra-stop

# Stop Registry
schema-registry/bin/schema-registry-stop

# Stop Kafka
kafka-server-stop

# Stop Zookeeper
zookeeper-server-stop
```

## Start the producer

This a standalone SpringBoot application leveraging on Apache Camel to implement integrations.
Configuration info are in `application.yml` file, make sure correct value are set for Kafka and DSE informations
You can setup the troughput of message but increasing or decreasing the `wait` value for each producer.

```yaml
# Externalize settings for PRODUCER
producer:
  csvFile: src/main/resources/exchange-data.csv
  kafka:
    server: localhost:9092
    schemaregistry: http://localhost:8081
    period: 2000
    ack: 1
    # Producer for string messages
    stringProducer:
      topicName: tick-stream
    # Producer for avro messages
    avroProducer:
      topicName: tick-stream-avro
    # Producer for JsonNode messages
    jsonProducer:
      topicName: tick-stream-json
    # Producer for Struct messages  
    structProducer:
      topicName: tick-stream-struct
```

This a standalone SpringBoot application leveraging on Apache Camel to implement

```bash
mvn spring-boot:run 
```

You should now see record in Dse tables.

## Start Web Application

Configuration is in `application.yml` file, make sure correct values are set for connecting to DataStax Enterprise Cluster

```
# Start Web Application
mvn spring-boot:run 
```
