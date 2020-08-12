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
- Kafka

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
   
2. ###### Properties file
     **src/main/resources/application.yml**
     ```
     spring:
       kafka:
         consumer:
           bootstrap-servers: localhost:9092
           group-id: group_id
         producer:
           bootstrap-servers: localhost:9092
         topic: message-topic
         superhero-topic: superhero-topic  
     ```
   
3. ###### Model class
     **com.arya.kafka.model.SuperHero.java**  
    ```
    public class SuperHero implements Serializable {
    
        private String name;
        private String superName;
        private String profession;
        private int age;
        private boolean canFly;
   
        // Constructor, Getter and Setter
    }
    ```

4. ###### Kafka Configuration
    **com.arya.kafka.config.KafkaProducerConfig.java** (`@Configuration` annotation on class)
    - Json Producer configuration
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
      
    - String Producer configuration
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
          
    **com.arya.kafka.config.KafkaConsumerConfig.java** (`@Configuration` annotation on class)
    - `@EnableKafka` annotation is mandatory to consume the message in config class or main class
  
    - Json Consumer configuration
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
   
    - String Consumer configuration
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
      
5. ###### Publishing data to Kafka Topic
    **com.arya.kafka.service.ProducerService.java**  
    - Publishing Json Object
        ```
        @Autowired
        private KafkaTemplate<String, T> kafkaTemplateSuperHero;
       
        public void sendSuperHeroMessage(T superHero) {
            logger.info("#### -> Publishing SuperHero :: {}", superHero);
            kafkaTemplateSuperHero.send(superHeroTopic, superHero);
        }
        ```
    - Publishing String message
        ```
        @Autowired
        private KafkaTemplate<String, String> kafkaTemplate;
        
        public void sendMessage(String message) {
            logger.info("#### -> Publishing message -> {}", message);
            kafkaTemplate.send(topic, message);
        }
        ```
      
6. ###### Consuming data from Kafka Topic
    **com.arya.kafka.service.ConsumerService.java**  
    ```
    // String Consumer
    @KafkaListener(topics = {"${spring.kafka.topic}"}, containerFactory = "kafkaListenerStringFactory", groupId = "group_id")
    public void consumeMessage(String message) {
        logger.info("**** -> Consumed message -> {}", message);
    }        
    
    // Object Consumer   
    @KafkaListener(topics = {"${spring.kafka.superhero-topic}"}, containerFactory = "kafkaListenerJsonFactory", groupId = "group_id")
    public void consumeSuperHero(SuperHero superHero) {
        logger.info("**** -> Consumed Super Hero :: {}", superHero);
    }
    ```
   
### API Endpoints

> **GET Mapping** http://localhost:8080/kafka/publish?message=test message 
   
> **POST Mapping** http://localhost:8080/kafka/publish  
                                                    
  Request Body  
  ```
    {
        "name": "Tony",
        "superName": "Iron Man",
        "profession": "Business",
        "age": 50,
        "canFly": true
    }
  ```
  
### Console Output
![Alt text](https://github.com/rahul-ghadge/spring-boot-kafka/blob/master/src/main/resources/Output.PNG?raw=true "Kafka Publisher-Consumer output")