package com.stationTracker.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component("kafka") // This ensures the kafka health indicator is registered as a bean
public class KafkaHealthIndicator implements HealthIndicator {

    private final KafkaAdmin kafkaAdmin;

    public KafkaHealthIndicator(KafkaAdmin kafkaAdmin) {
        this.kafkaAdmin = kafkaAdmin;
    }

    @Override
    public Health health() {
        try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            DescribeClusterResult cluster = adminClient.describeCluster();
            String clusterId = cluster.clusterId().get(2, TimeUnit.SECONDS); // 2s timeout

            return Health.up()
                    .withDetail("clusterId", clusterId)
                    .withDetail("brokerCount", cluster.nodes().get().size())
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", "Could not connect to Kafka broker at kafka:9092")
                    .withException(e)
                    .build();
        }
    }
}