# spring-boot-kafka
Spring boot kafka with Super Hero - This project explains How to **publish** message in kafka Topic 
and **consume** a message from Kafka Topic. Here message is in String and Json Object format.


## Prerequisites 
- Java
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Maven](https://maven.apache.org/guides/index.html)
- [Kafka](https://kafka.apache.org/documentation/)

## Tools
- Eclipse or IntelliJ IDEA (or any preferred IDE) with embedded Gradle
- Maven (version >= 3.6.0)
- Postman (or any RESTful API testing tool)

<br/>

### Install Zookeeper
Step 1: Download apache-zookeeper-x.x.x from [Zookeeper site](https://zookeeper.apache.org/releases.html)

Step 2: Extract the folder at location c:\apache-zookeeper-x.x.x

Step 3: Add c:\apache-zookeeper-x.x.x\bin path as environment variable.
 


### Install Kafka
Step 1: Download kafka_x.xx-x.x.x from [Apache kafka site](https://kafka.apache.org)

Step 2: Extract the folder at location c:\kafka_x.xx-x.x.x

Step 3: Add c:\kafka_x.xx-x.x.x\bin path as environment variable.


##### Start the ZooKeeper and Kafka Server by using the below command


##### Start ZooKeeper
Go to the kafka location using terminal and hit below command like 
> `cd c:\apache-zookeeper-x.x.x`  
> `zkserver`

or
> `.\bin\zookeeper-server-start.sh .\config\zookeeper.properties`

If no error on the console means Zookeeper is started and running.

##### Start Kafka Server
Go to the kafka location using terminal and hit below command like 
> `cd c:\kafka_x.xx-x.x.x`

> `.\bin\kafka-server-start.sh .\config\server.properties`

If no error on the console means Apache Kafka is started and running now.


### Maven Dependencies
**pom.xml**  
```
<dependency>
    <groupId>org.springframework.kafka</groupId>
	<artifactId>spring-kafka</artifactId>
</dependency>
```