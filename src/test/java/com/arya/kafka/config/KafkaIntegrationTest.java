//package com.arya.kafka.config;
//
//import com.arya.kafka.service.ProducerService;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
//import org.springframework.kafka.listener.ContainerProperties;
//import org.springframework.kafka.listener.KafkaMessageListenerContainer;
//import org.springframework.kafka.listener.MessageListener;
//import org.springframework.kafka.test.EmbeddedKafkaBroker;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.kafka.test.utils.ContainerTestUtils;
//import org.springframework.kafka.test.utils.KafkaTestUtils;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//
//import java.util.Map;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
///**
// * Integration tests for the Kafka producer-consumer pipeline using an embedded Kafka broker.
// *
// * <p>Uses {@link EmbeddedKafka} to spin up an in-process broker, allowing full round-trip
// * testing without requiring an external Kafka cluster.
// */
//@SpringBootTest
//@ActiveProfiles("test")
//@DirtiesContext
//@EmbeddedKafka(
//        partitions       = 1,
//        topics           = {
//                "message-topic",
//                "superhero-topic",
//                "notification-topic",
//                "audit-topic",
//                "message-topic.DLT",
//                "superhero-topic.DLT"
//        }
//)
//@TestPropertySource(properties = {
//        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
//        "spring.kafka.consumer.group-id=integration-test-group",
//        "spring.kafka.consumer.auto-offset-reset=earliest",
//        "spring.kafka.consumer.enable-auto-commit=true",
//        "kafka.retry.max-attempts=1",
//        "kafka.retry.initial-interval-ms=100",
//        "kafka.retry.multiplier=1.0",
//        "kafka.retry.max-interval-ms=100"
//})
//class KafkaIntegrationTest {
//
//    @Autowired
//    private ProducerService producerService;
//
//    @Autowired
//    private EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    private KafkaMessageListenerContainer<String, String> container;
//    private BlockingQueue<ConsumerRecord<String, String>> records;
//
//    @BeforeEach
//    void setUp() {
//        records = new LinkedBlockingQueue<>();
//
//        Map<String, Object> consumerProperties =
//                KafkaTestUtils.consumerProps("integration-test-group", "true", embeddedKafkaBroker);
//        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
//        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,        "earliest");
//
//        DefaultKafkaConsumerFactory<String, String> consumerFactory =
//                new DefaultKafkaConsumerFactory<>(consumerProperties);
//
//        ContainerProperties containerProperties = new ContainerProperties("message-topic");
//        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
//        container.setupMessageListener((MessageListener<String, String>) records::add);
//        container.start();
//
//        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
//    }
//
//    @AfterEach
//    void tearDown() {
//        container.stop();
//    }
//
//    @Test
//    @DisplayName("Published string message should be received by test listener")
//    void publishMessage_shouldBeConsumedByTestListener() throws InterruptedException {
//        String testMessage = "integration-test-message";
//
//        producerService.sendMessage(testMessage);
//
//        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
//
//        assertThat(received).isNotNull();
//        assertThat(received.value()).isEqualTo(testMessage);
//        assertThat(received.topic()).isEqualTo("message-topic");
//    }
//
//    @Test
//    @DisplayName("Published message should include correlationId header")
//    void publishMessage_shouldIncludeCorrelationIdHeader() throws InterruptedException {
//        producerService.sendMessage("header-test");
//
//        ConsumerRecord<String, String> received = records.poll(10, TimeUnit.SECONDS);
//
//        assertThat(received).isNotNull();
//        assertThat(received.headers().lastHeader("correlationId")).isNotNull();
//    }
//}
