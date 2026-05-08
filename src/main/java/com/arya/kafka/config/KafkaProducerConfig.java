package com.arya.kafka.config;

import com.arya.kafka.model.SuperHero;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Enterprise Kafka producer configuration.
 *
 * <p>Three producer factories are registered:
 * <ul>
 *   <li><b>kafkaTemplate</b>          — {@code @Primary} generic Object producer used by
 *       {@link org.springframework.kafka.listener.DeadLetterPublishingRecoverer}.</li>
 *   <li><b>superHeroKafkaTemplate</b> — typed JSON producer for {@link SuperHero} events.</li>
 *   <li><b>kafkaStringTemplate</b>    — plain String producer for lightweight messages.</li>
 * </ul>
 *
 * <p>All producers use {@code acks=all} and {@code enable.idempotence=true} to guarantee
 * exactly-once delivery semantics within a single producer session.
 *
 * @author rahul-ghadge
 */
@Slf4j
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ─── Primary / Generic JSON Producer (used by DLQ recoverer) ─────────────

    @Bean
    @Primary
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = commonProducerProperties();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    // ─── SuperHero Typed JSON Producer ────────────────────────────────────────

    @Bean
    public ProducerFactory<String, SuperHero> superHeroProducerFactory() {
        Map<String, Object> config = commonProducerProperties();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, SuperHero> superHeroKafkaTemplate() {
        KafkaTemplate<String, SuperHero> template = new KafkaTemplate<>(superHeroProducerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    // ─── String Producer ──────────────────────────────────────────────────────

    @Bean
    public ProducerFactory<String, String> stringProducerFactory() {
        Map<String, Object> config = commonProducerProperties();
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaStringTemplate() {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(stringProducerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    // ─── Shared Producer Properties ───────────────────────────────────────────

    private Map<String, Object> commonProducerProperties() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        config.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120_000);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16_384);
        return config;
    }
}
