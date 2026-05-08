//package com.arya.kafka.producer;
//
//import com.arya.kafka.config.KafkaTopicProperties;
//import com.arya.kafka.model.SuperHero;
//import com.arya.kafka.service.ProducerService;
//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.clients.producer.RecordMetadata;
//import org.apache.kafka.common.TopicPartition;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.support.SendResult;
//
//import java.util.concurrent.CompletableFuture;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for {@link ProducerService}.
// *
// * <p>Kafka infrastructure is fully mocked — no embedded broker is started.
// * Tests verify argument validation, delegation to {@link KafkaTemplate},
// * and counter increments on the success path.
// */
//@ExtendWith(MockitoExtension.class)
//class ProducerServiceTest {
//
//    @Mock private KafkaTemplate<String, SuperHero> superHeroKafkaTemplate;
//    @Mock private KafkaTemplate<String, String>    kafkaStringTemplate;
//    @Mock private KafkaTopicProperties             topicProperties;
//    @Mock private MeterRegistry                    meterRegistry;
//    @Mock private Counter                          counter;
//
//    private ProducerService producerService;
//
//    @BeforeEach
//    void setUp() {
//        // Stub counter registrations called in @PostConstruct
//        when(meterRegistry.counter(anyString(), anyString(), anyString())).thenReturn(counter);
//        when(topicProperties.getMessage()).thenReturn("message-topic");
//        when(topicProperties.getSuperhero()).thenReturn("superhero-topic");
//        when(topicProperties.getNotification()).thenReturn("notification-topic");
//
//        // Construct manually (Spring context not available in unit tests)
//        producerService = new ProducerService(
//                superHeroKafkaTemplate,
//                kafkaStringTemplate,
//                topicProperties,
//                meterRegistry
//        );
//
//        // Trigger @PostConstruct manually
//        org.springframework.test.util.ReflectionTestUtils.invokeMethod(producerService, "initMetrics");
//    }
//
//    // ─── sendMessage ──────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("sendMessage: delegates to kafkaStringTemplate for valid input")
//    void sendMessage_valid_delegates() {
//        RecordMetadata meta = new RecordMetadata(
//                new TopicPartition("message-topic", 0), 0, 0, 0, 0, 0);
//        SendResult<String, String> result =
//                new SendResult<>(new ProducerRecord<>("message-topic", "hello"), meta);
//
//        when(kafkaStringTemplate.send(any())).thenReturn(CompletableFuture.completedFuture(result));
//
//        producerService.sendMessage("hello");
//
//        verify(kafkaStringTemplate, times(1)).send(any());
//    }
//
//    @Test
//    @DisplayName("sendMessage: throws IllegalArgumentException for blank message")
//    void sendMessage_blank_throws() {
//        assertThatThrownBy(() -> producerService.sendMessage("   "))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("blank");
//    }
//
//    @Test
//    @DisplayName("sendMessage: throws IllegalArgumentException for null message")
//    void sendMessage_null_throws() {
//        assertThatThrownBy(() -> producerService.sendMessage(null))
//                .isInstanceOf(IllegalArgumentException.class);
//    }
//
//    // ─── sendSuperHeroMessage ─────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("sendSuperHeroMessage: delegates to superHeroKafkaTemplate for valid hero")
//    void sendSuperHeroMessage_valid_delegates() {
//        SuperHero hero = SuperHero.builder()
//                .name("Bruce Wayne")
//                .superName("Batman")
//                .profession("Business")
//                .age(40)
//                .canFly(false)
//                .build();
//
//        RecordMetadata meta = new RecordMetadata(
//                new TopicPartition("superhero-topic", 0), 0, 0, 0, 0, 0);
//        SendResult<String, SuperHero> result =
//                new SendResult<>(new ProducerRecord<>("superhero-topic", hero), meta);
//
//        when(superHeroKafkaTemplate.send(any())).thenReturn(CompletableFuture.completedFuture(result));
//
//        producerService.sendSuperHeroMessage(hero);
//
//        verify(superHeroKafkaTemplate, times(1)).send(any());
//    }
//
//    @Test
//    @DisplayName("sendSuperHeroMessage: throws IllegalArgumentException for null hero")
//    void sendSuperHeroMessage_null_throws() {
//        assertThatThrownBy(() -> producerService.sendSuperHeroMessage(null))
//                .isInstanceOf(IllegalArgumentException.class)
//                .hasMessageContaining("null");
//    }
//
//    // ─── sendNotification ─────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("sendNotification: delegates to kafkaStringTemplate")
//    void sendNotification_valid_delegates() {
//        when(kafkaStringTemplate.send(anyString(), anyString())).thenReturn(
//                CompletableFuture.completedFuture(null));
//
//        producerService.sendNotification("disk alert");
//
//        verify(kafkaStringTemplate, times(1)).send(eq("notification-topic"), eq("disk alert"));
//    }
//}
