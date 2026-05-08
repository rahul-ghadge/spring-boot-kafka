package com.arya.kafka.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Programmatic Kafka topic creation via Spring's {@link org.springframework.kafka.config.TopicBuilder}.
 *
 * <p>Topics are created automatically on application startup if they do not yet exist.
 * Enterprise topics are configured with 3 partitions and a replication factor of 1
 * (adjust replication factor to ≥ 3 in production clusters).
 *
 * @author rahul-ghadge
 */
@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicProperties topicProperties;

    @Bean
    public NewTopic messageTopic() {
        return TopicBuilder.name(topicProperties.getMessage())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic superheroTopic() {
        return TopicBuilder.name(topicProperties.getSuperhero())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationTopic() {
        return TopicBuilder.name(topicProperties.getNotification())
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic auditTopic() {
        return TopicBuilder.name(topicProperties.getAudit())
                .partitions(1)
                .replicas(1)
                .build();
    }

    // ─── Dead-Letter Queue Topics ──────────────────────────────────────────────

    @Bean
    public NewTopic messageDeadLetterTopic() {
        return TopicBuilder.name(topicProperties.getMessageDlt())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic superheroDeadLetterTopic() {
        return TopicBuilder.name(topicProperties.getSuperheroDlt())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
