package com.arya.kafka.service;

import com.arya.kafka.config.KafkaTopicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service responsible for publishing structured audit log entries to the dedicated audit topic.
 *
 * <p>Audit events are published asynchronously so they do not block the critical consumer
 * processing path. Each entry captures the event type, a brief payload summary, a correlation
 * ID for cross-service tracing, and an ISO-8601 timestamp.
 *
 * @author rahul-ghadge
 */
@Slf4j
@Service
public class AuditService {

    private final KafkaTemplate<String, String> kafkaStringTemplate;
    private final KafkaTopicProperties          topicProperties;

    public AuditService(
            @Qualifier("kafkaStringTemplate") KafkaTemplate<String, String> kafkaStringTemplate,
            KafkaTopicProperties topicProperties) {
        this.kafkaStringTemplate = kafkaStringTemplate;
        this.topicProperties     = topicProperties;
    }

    /**
     * Asynchronously publishes an audit record to the audit Kafka topic.
     *
     * @param eventType      the type of event (e.g., {@code MESSAGE_CONSUMED})
     * @param payloadSummary a brief, non-sensitive summary of the processed payload
     * @param correlationId  the correlation ID from the originating message header
     */
    @Async
    public void recordAudit(String eventType, String payloadSummary, String correlationId) {
        String sanitised = payloadSummary.replace("\"", "'");
        String auditEntry = String.format(
                "{\"eventType\":\"%s\",\"correlationId\":\"%s\",\"summary\":\"%s\",\"timestamp\":\"%s\"}",
                eventType, correlationId, sanitised, Instant.now()
        );

        log.debug("Publishing audit entry [eventType={}] [correlationId={}]", eventType, correlationId);
        kafkaStringTemplate.send(topicProperties.getAudit(), correlationId, auditEntry);
    }
}
