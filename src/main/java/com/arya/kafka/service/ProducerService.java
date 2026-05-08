package com.arya.kafka.service;

import com.arya.kafka.config.KafkaTopicProperties;
import com.arya.kafka.exception.KafkaPublishException;
import com.arya.kafka.model.SuperHero;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Enterprise Kafka producer service responsible for publishing domain events to Kafka topics.
 *
 * <p>Features:
 * <ul>
 *   <li>Assigns a unique {@code correlationId} to every message header for distributed tracing.</li>
 *   <li>Publishes asynchronously and handles callback success/failure logging.</li>
 *   <li>Records per-topic publish success and failure Micrometer counters.</li>
 *   <li>Wraps infrastructure exceptions in {@link KafkaPublishException}.</li>
 * </ul>
 *
 * @author rahul-ghadge
 */
@Slf4j
@Service
public class ProducerService {

    private final KafkaTemplate<String, SuperHero> superHeroKafkaTemplate;
    private final KafkaTemplate<String, String>    kafkaStringTemplate;
    private final KafkaTopicProperties             topicProperties;
    private final MeterRegistry                    meterRegistry;

    private Counter messagePublishSuccessCounter;
    private Counter messagePublishFailureCounter;
    private Counter superheroPublishSuccessCounter;
    private Counter superheroPublishFailureCounter;

    // Explicit constructor so we can use @Qualifier on the SuperHero template bean
    public ProducerService(
            @Qualifier("superHeroKafkaTemplate") KafkaTemplate<String, SuperHero> superHeroKafkaTemplate,
            @Qualifier("kafkaStringTemplate")    KafkaTemplate<String, String>    kafkaStringTemplate,
            KafkaTopicProperties topicProperties,
            MeterRegistry meterRegistry) {

        this.superHeroKafkaTemplate = superHeroKafkaTemplate;
        this.kafkaStringTemplate    = kafkaStringTemplate;
        this.topicProperties        = topicProperties;
        this.meterRegistry          = meterRegistry;
    }

    @PostConstruct
    private void initMetrics() {
        messagePublishSuccessCounter   = meterRegistry.counter("kafka.publish.success", "topic", topicProperties.getMessage());
        messagePublishFailureCounter   = meterRegistry.counter("kafka.publish.failure", "topic", topicProperties.getMessage());
        superheroPublishSuccessCounter = meterRegistry.counter("kafka.publish.success", "topic", topicProperties.getSuperhero());
        superheroPublishFailureCounter = meterRegistry.counter("kafka.publish.failure", "topic", topicProperties.getSuperhero());
    }

    // ─── String Message Publishing ─────────────────────────────────────────────

    /**
     * Publishes a plain-text message to the message topic.
     *
     * @param message the string payload to publish
     * @throws IllegalArgumentException if the message is blank
     */
    public void sendMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message payload must not be blank");
        }

        String correlationId = UUID.randomUUID().toString();
        log.info("#### -> Publishing message [correlationId={}] -> {}", correlationId, message);

        Message<String> kafkaMessage = MessageBuilder
                .withPayload(message)
                .setHeader(KafkaHeaders.TOPIC, topicProperties.getMessage())
                .setHeader("correlationId", correlationId)
                .build();

        CompletableFuture<SendResult<String, String>> future = kafkaStringTemplate.send(kafkaMessage);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                messagePublishFailureCounter.increment();
                log.error("#### -> Failed to publish message [correlationId={}]: {}", correlationId, ex.getMessage());
            } else {
                messagePublishSuccessCounter.increment();
                log.debug("#### -> Message published [correlationId={}] partition={} offset={}",
                        correlationId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // ─── SuperHero JSON Publishing ─────────────────────────────────────────────

    /**
     * Publishes a {@link SuperHero} domain object as a JSON event to the superhero topic.
     *
     * @param superHero the domain object to publish
     * @throws IllegalArgumentException if superHero is null
     */
    public void sendSuperHeroMessage(SuperHero superHero) {
        if (superHero == null) {
            throw new IllegalArgumentException("SuperHero payload must not be null");
        }

        String correlationId = UUID.randomUUID().toString();
        log.info("#### -> Publishing SuperHero [correlationId={}] :: {}", correlationId, superHero);

        Message<SuperHero> kafkaMessage = MessageBuilder
                .withPayload(superHero)
                .setHeader(KafkaHeaders.TOPIC, topicProperties.getSuperhero())
                .setHeader("correlationId", correlationId)
                .build();

        CompletableFuture<SendResult<String, SuperHero>> future = superHeroKafkaTemplate.send(kafkaMessage);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                superheroPublishFailureCounter.increment();
                log.error("#### -> Failed to publish SuperHero [correlationId={}]: {}", correlationId, ex.getMessage());
                throw new KafkaPublishException("Failed to publish SuperHero event", ex);
            } else {
                superheroPublishSuccessCounter.increment();
                log.debug("#### -> SuperHero published [correlationId={}] partition={} offset={}",
                        correlationId,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    // ─── Notification Publishing ───────────────────────────────────────────────

    /**
     * Publishes a notification message to the notification topic.
     *
     * @param notification the notification payload
     */
    public void sendNotification(String notification) {
        log.info("#### -> Publishing notification -> {}", notification);
        kafkaStringTemplate.send(topicProperties.getNotification(), notification);
    }
}
