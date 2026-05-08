package com.arya.kafka.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 metadata configuration for Swagger UI documentation.
 *
 * <p>Accessible at {@code /swagger-ui.html} once the application is running.
 *
 * @author rahul-ghadge
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:spring-boot-kafka}")
    private String applicationName;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("http://localhost:8080").description("Docker")
                ))
                .components(new Components());
    }

    private Info apiInfo() {
        return new Info()
                .title("Spring Boot Kafka Enterprise API")
                .description("""
                        Enterprise-grade Spring Boot Kafka application demonstrating:
                        - Multiple typed Producers (String and JSON)
                        - Multiple concurrent Consumers with manual offset management
                        - Dead-Letter Queue (DLQ) routing for failed messages
                        - Exponential backoff retry policies
                        - Prometheus metrics and Spring Actuator health checks
                        - Structured audit logging via a dedicated Kafka topic
                        """)
                .version("2.0.0")
                .contact(new Contact()
                        .name("rahul-ghadge")
                        .url("https://github.com/rahul-ghadge/spring-boot-kafka"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"));
    }
}
