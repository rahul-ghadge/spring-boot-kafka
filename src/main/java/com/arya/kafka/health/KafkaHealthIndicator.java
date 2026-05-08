package com.arya.kafka.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Custom Spring Actuator health indicator for the Kafka cluster.
 *
 * <p>Queries the Kafka Admin API for basic cluster metadata (node count, cluster ID)
 * and surfaces the result at {@code /actuator/health}. A timeout of 5 seconds is
 * applied to prevent slow broker responses from blocking the health endpoint.
 *
 * @author rahul-ghadge
 */
@Slf4j
@Component("kafkaCluster")
@RequiredArgsConstructor
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    private static final int TIMEOUT_SECONDS = 5;

    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult clusterResult = adminClient.describeCluster();
            String clusterId  = clusterResult.clusterId().get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            int nodeCount     = clusterResult.nodes().get(TIMEOUT_SECONDS, TimeUnit.SECONDS).size();

            return Health.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("nodeCount", nodeCount)
                    .withDetail("status", "Kafka cluster is reachable")
                    .build();

        } catch (Exception ex) {
            log.error("Kafka health check failed: {}", ex.getMessage());
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .withDetail("status", "Kafka cluster is unreachable")
                    .build();
        }
    }
}
