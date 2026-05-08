package com.arya.kafka.config;

import com.arya.kafka.model.SuperHero;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Kafka consumer configuration.
 *
 * <p>Two listener container factories are registered:
 * <ul>
 *   <li><b>kafkaListenerJsonFactory</b>   — for typed JSON domain objects ({@link SuperHero}).</li>
 *   <li><b>kafkaListenerStringFactory</b> — for plain-text string messages.</li>
 * </ul>
 *
 * <p>Both factories share enterprise-grade behaviours:
 * <ul>
 *   <li>Manual offset commit ({@code MANUAL_IMMEDIATE}) to prevent message loss.</li>
 *   <li>Exponential backoff retry before routing to the Dead-Letter Queue.</li>
 *   <li>Concurrency of 3 threads per container for parallel partition consumption.</li>
 * </ul>
 *
 * @author rahul-ghadge
 */
@Slf4j
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.retry.max-attempts:3}")
    private long maxRetryAttempts;

    @Value("${kafka.retry.initial-interval-ms:1000}")
    private long initialIntervalMs;

    @Value("${kafka.retry.multiplier:2.0}")
    private double multiplier;

    @Value("${kafka.retry.max-interval-ms:10000}")
    private long maxIntervalMs;

    // ─── JSON Consumer ─────────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, SuperHero> consumerFactory() {
        JsonDeserializer<SuperHero> deserializer = new JsonDeserializer<>(SuperHero.class, false);
        deserializer.addTrustedPackages("com.arya.kafka.*");

        return new DefaultKafkaConsumerFactory<>(
                commonConsumerProperties(),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SuperHero> kafkaListenerJsonFactory(
            // Inject the @Primary KafkaTemplate<String, Object> by type — no qualifier needed
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, SuperHero> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(defaultErrorHandler(kafkaTemplate));
        return factory;
    }

    // ─── String Consumer ───────────────────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, String> stringConsumerFactory() {
        Map<String, Object> config = commonConsumerProperties();
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerStringFactory(
            KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(stringConsumerFactory());
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setCommonErrorHandler(defaultErrorHandler(kafkaTemplate));
        return factory;
    }

    // ─── Error Handling: Retry + DLQ ──────────────────────────────────────────

    /**
     * Configures an exponential backoff retry policy followed by Dead-Letter Queue routing.
     *
     * <p>After all retry attempts are exhausted the record is forwarded to the corresponding
     * {@code *.DLT} topic for manual inspection and reprocessing.
     *
     * @param kafkaTemplate the primary {@code KafkaTemplate<String, Object>} used by the recoverer
     */
    @Bean
    public DefaultErrorHandler defaultErrorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        ExponentialBackOff backOff = new ExponentialBackOff(initialIntervalMs, multiplier);
        backOff.setMaxInterval(maxIntervalMs);
        backOff.setMaxElapsedTime(maxRetryAttempts * maxIntervalMs);

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }

    // ─── Shared Consumer Properties ───────────────────────────────────────────

    private Map<String, Object> commonConsumerProperties() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        return config;
    }
}
