package com.stationTracker.controller;

import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    public HealthController(HealthEndpoint healthEndpoint) {
        this.healthEndpoint = healthEndpoint;
    }

    @GetMapping("/api/system/status")
    public Map<String, String> getSystemStatus() {
        // Get the overall health (UP/DOWN)
        HealthComponent overallHealth = healthEndpoint.health();

        return Map.of(
                "overall", overallHealth.getStatus().getCode(),
                "database", getComponentStatus("db"),
                "kafka", getComponentStatus("kafka")
        );
    }

    private String getComponentStatus(String componentName) {
        // Logic to safely check sub-components like 'db' or 'kafka'
        return Optional.ofNullable(healthEndpoint.healthForPath(componentName))
                .map(c -> c.getStatus().getCode())
                .orElse("UNKNOWN");
    }

    @GetMapping("/api/system/debug")
    public Object debugHealth() {
        // This will show you exactly what keys are available in the registry
        return healthEndpoint.health();
    }
}