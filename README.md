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


### Code Snippets
1. ###### Maven Dependencies
     **pom.xml**  
    ```
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    ```

2. ###### Kafka Configuration
    **com.arya.kafka.config.KafkaProducerConfig.java**
    - Json Producer
      ```
      configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
      ```
      ```
      @Bean
      public <T> KafkaTemplate<String, T> kafkaTemplate() {
          return new KafkaTemplate<>(producerFactory());
      }  
      ``` 
      
    - String Producer
      ```
      configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      ```
      ```
      @Bean
      public KafkaTemplate<String, String> kafkaStringTemplate() {
          return new KafkaTemplate<>(producerStringFactory());
      }
      ```
          
    **com.arya.kafka.config.KafkaConsumerConfig.java**
    - **`@EnableKafka`**annotation is mandatory to consume the message in config class or main class
  
    - Json Consumer
      ```
      @Bean
      public ConsumerFactory<String, SuperHero> consumerFactory() {
         Map<String, Object> config = new HashMap<>();
     
         config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
         config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
         config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
         config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
     
         return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new JsonDeserializer<>(SuperHero.class));
      }
      
      @Bean
      public <T> ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerJsonFactory() {
         ConcurrentKafkaListenerContainerFactory<String, SuperHero> factory = new ConcurrentKafkaListenerContainerFactory<>();
         factory.setConsumerFactory(consumerFactory());
         factory.setMessageConverter(new StringJsonMessageConverter());
         factory.setBatchListener(true);
         return factory;
      }     
   
    - String Consumer
      ``` 
      @Bean
      public ConsumerFactory<String, String> stringConsumerFactory() {
         Map<String, Object> config = new HashMap<>();
   
         config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
         config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
         config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
         config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
         config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
         config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
   
         return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), new StringDeserializer());
      }
   
      @Bean
      public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerStringFactory() {
         ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
         factory.setConsumerFactory(stringConsumerFactory());
         factory.setBatchListener(true);
         return factory;
      }

      