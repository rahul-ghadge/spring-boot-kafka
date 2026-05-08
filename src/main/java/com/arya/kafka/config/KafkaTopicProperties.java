package com.arya.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Type-safe binding of all Kafka topic names from {@code application.yml}.
 *
 * <p>Centralises topic name management to eliminate hardcoded string literals
 * scattered across producer and consumer beans.
 *
 * @author rahul-ghadge
 */
@Getter
@Setter
@Validated
@Configuration
@ConfigurationProperties(prefix = "spring.kafka.topics")
public class KafkaTopicProperties {

    @NotBlank
    private String message = "message-topic";

    @NotBlank
    private String superhero = "superhero-topic";

    @NotBlank
    private String notification = "notification-topic";

    @NotBlank
    private String audit = "audit-topic";

    @NotBlank
    private String messageDlt = "message-topic.DLT";

    @NotBlank
    private String superheroDlt = "superhero-topic.DLT";
}
