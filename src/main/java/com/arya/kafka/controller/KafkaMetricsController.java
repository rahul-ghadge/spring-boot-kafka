package com.arya.kafka.controller;

import com.arya.kafka.dto.ApiResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller exposing a summary of Kafka producer and consumer Micrometer counters.
 *
 * <p>Complements the raw Prometheus scrape endpoint ({@code /actuator/prometheus}) with a
 * human-readable JSON view of the key operational metrics.
 *
 * @author rahul-ghadge
 */
@RestController
@RequestMapping("/kafka/metrics")
@RequiredArgsConstructor
@Tag(name = "Kafka Metrics", description = "Operational metrics for Kafka producer and consumer activity")
public class KafkaMetricsController {

    private final MeterRegistry meterRegistry;

    @Operation(
            summary     = "Get Kafka operational metrics",
            description = "Returns publish success/failure and consume success/failure counters for all registered topics."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Double>>> getMetrics() {
        Map<String, Double> metrics = new HashMap<>();

        safeCount(metrics, "kafka.publish.success", "topic", "message-topic");
        safeCount(metrics, "kafka.publish.failure", "topic", "message-topic");
        safeCount(metrics, "kafka.publish.success", "topic", "superhero-topic");
        safeCount(metrics, "kafka.publish.failure", "topic", "superhero-topic");
        safeCount(metrics, "kafka.consume.success", "topic", "message-topic");
        safeCount(metrics, "kafka.consume.failure", "topic", "message-topic");
        safeCount(metrics, "kafka.consume.success", "topic", "superhero-topic");
        safeCount(metrics, "kafka.consume.failure", "topic", "superhero-topic");
        safeCount(metrics, "kafka.dlq.received",    "topic", "message-topic.DLT");
        safeCount(metrics, "kafka.dlq.received",    "topic", "superhero-topic.DLT");

        return ResponseEntity.ok(ApiResponse.success("Kafka metrics retrieved successfully", metrics));
    }

    /**
     * Safely retrieves a counter value, defaulting to 0.0 if the counter has not yet been registered.
     */
    private void safeCount(Map<String, Double> target, String metricName, String tagKey, String tagValue) {
        try {
            Counter counter = meterRegistry.get(metricName).tag(tagKey, tagValue).counter();
            target.put(metricName + "[" + tagValue + "]", counter.count());
        } catch (Exception ignored) {
            target.put(metricName + "[" + tagValue + "]", 0.0);
        }
    }
}
