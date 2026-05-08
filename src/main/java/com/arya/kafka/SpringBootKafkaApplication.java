package com.arya.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Spring Boot Kafka Enterprise application.
 *
 * <p>This application demonstrates enterprise-grade Kafka integration with:
 * <ul>
 *   <li>Multiple typed producers (String and JSON)</li>
 *   <li>Multiple concurrent consumers with manual offset management</li>
 *   <li>Dead-Letter Queue (DLQ) for failed message handling</li>
 *   <li>Retry policies with exponential backoff</li>
 *   <li>Prometheus metrics and Spring Actuator health endpoints</li>
 *   <li>OpenAPI documentation via Swagger UI</li>
 * </ul>
 *
 * @author rahul-ghadge
 * @version 2.0.0
 */
@SpringBootApplication
@EnableKafka
@EnableAsync
public class SpringBootKafkaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootKafkaApplication.class, args);
    }
}
