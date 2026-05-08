package com.arya.kafka.service;

import com.arya.kafka.config.KafkaTopicProperties;
import com.arya.kafka.model.SuperHero;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Enterprise Kafka consumer service processing events from all registered topics.
 *
 * <p>Features:
 * <ul>
 *   <li>Manual offset acknowledgement via {@link Acknowledgment} to prevent data loss.</li>
 *   <li>Correlation ID extraction from message headers for distributed tracing.</li>
 *   <li>Per-topic consumed/failed Micrometer counters.</li>
 *   <li>Dedicated DLQ listeners to monitor dead-lettered records.</li>
 * </ul>
 *
 * @author rahul-ghadge
 */
@Slf4j
@Service
public class ConsumerService {

    private final MeterRegistry        meterRegistry;
    private final KafkaTopicProperties topicProperties;
    private final AuditService         auditService;

    private Counter messageConsumedCounter;
    private Counter messageFailedCounter;
    private Counter superheroConsumedCounter;
    private Counter superheroFailedCounter;

    public ConsumerService(MeterRegistry meterRegistry,
                           KafkaTopicProperties topicProperties,
                           AuditService auditService) {
        this.meterRegistry   = meterRegistry;
        this.topicProperties = topicProperties;
        this.auditService    = auditService;
    }

    @PostConstruct
    private void initMetrics() {
        messageConsumedCounter   = meterRegistry.counter("kafka.consume.success", "topic", topicProperties.getMessage());
        messageFailedCounter     = meterRegistry.counter("kafka.consume.failure", "topic", topicProperties.getMessage());
        superheroConsumedCounter = meterRegistry.counter("kafka.consume.success", "topic", topicProperties.getSuperhero());
        superheroFailedCounter   = meterRegistry.counter("kafka.consume.failure", "topic", topicProperties.getSuperhero());
    }

    // ─── String Message Consumer ───────────────────────────────────────────────

    @KafkaListener(
            topics           = {"${spring.kafka.topics.message}"},
            groupId          = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerStringFactory"
    )
    public void consumeMessage(
            @Payload String message,
            @Header(value = "correlationId", required = false) String correlationId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        String corrId = Optional.ofNullable(correlationId).orElse("N/A");
        log.info("**** -> Consumed message [correlationId={}] partition={} offset={} -> {}",
                corrId, partition, offset, message);

        try {
            processMessage(message);
            auditService.recordAudit("MESSAGE_CONSUMED", message, corrId);
            messageConsumedCounter.increment();
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            messageFailedCounter.increment();
            log.error("**** -> Failed to process message [correlationId={}]: {}", corrId, ex.getMessage(), ex);
            throw ex;
        }
    }

    // ─── SuperHero JSON Consumer ───────────────────────────────────────────────

    @KafkaListener(
            topics           = {"${spring.kafka.topics.superhero}"},
            groupId          = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerJsonFactory"
    )
    public void consumeSuperHero(
            @Payload SuperHero superHero,
            @Header(value = "correlationId", required = false) String correlationId,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        String corrId = Optional.ofNullable(correlationId).orElse("N/A");
        log.info("**** -> Consumed SuperHero [correlationId={}] partition={} offset={} :: {}",
                corrId, partition, offset, superHero);

        try {
            processSuperHero(superHero);
            auditService.recordAudit("SUPERHERO_CONSUMED", superHero.toString(), corrId);
            superheroConsumedCounter.increment();
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            superheroFailedCounter.increment();
            log.error("**** -> Failed to process SuperHero [correlationId={}]: {}", corrId, ex.getMessage(), ex);
            throw ex;
        }
    }

    // ─── Notification Consumer ─────────────────────────────────────────────────

    @KafkaListener(
            topics           = {"${spring.kafka.topics.notification}"},
            groupId          = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerStringFactory"
    )
    public void consumeNotification(
            @Payload String notification,
            Acknowledgment acknowledgment) {

        log.info("**** -> Consumed notification -> {}", notification);
        try {
            processNotification(notification);
            acknowledgment.acknowledge();
        } catch (Exception ex) {
            log.error("**** -> Failed to process notification: {}", ex.getMessage(), ex);
            throw ex;
        }
    }

    // ─── Dead-Letter Queue Listeners ──────────────────────────────────────────

    @KafkaListener(
            topics           = {"${spring.kafka.topics.message-dlt}"},
            groupId          = "${spring.kafka.consumer.group-id}-dlt",
            containerFactory = "kafkaListenerStringFactory"
    )
    public void consumeMessageDlt(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment) {

        log.error("!!!! -> DLQ record received [topic={}] partition={} offset={} -> payload={}",
                record.topic(), record.partition(), record.offset(), record.value());
        meterRegistry.counter("kafka.dlq.received", "topic", topicProperties.getMessageDlt()).increment();
        acknowledgment.acknowledge();
    }

    @KafkaListener(
            topics           = {"${spring.kafka.topics.superhero-dlt}"},
            groupId          = "${spring.kafka.consumer.group-id}-dlt",
            containerFactory = "kafkaListenerStringFactory"
    )
    public void consumeSuperHeroDlt(
            ConsumerRecord<String, String> record,
            Acknowledgment acknowledgment) {

        log.error("!!!! -> DLQ record received [topic={}] partition={} offset={} -> payload={}",
                record.topic(), record.partition(), record.offset(), record.value());
        meterRegistry.counter("kafka.dlq.received", "topic", topicProperties.getSuperheroDlt()).increment();
        acknowledgment.acknowledge();
    }

    // ─── Business Processing Methods ──────────────────────────────────────────

    private void processMessage(String message) {
        log.debug("Processing string message: {}", message);
    }

    private void processSuperHero(SuperHero superHero) {
        log.debug("Processing SuperHero: name={}, superName={}", superHero.getName(), superHero.getSuperName());
    }

    private void processNotification(String notification) {
        log.debug("Processing notification: {}", notification);
    }
}
