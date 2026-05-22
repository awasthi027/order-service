package com.ashi.orderservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${spring.application.name:order-service}")
    private String appName;

    @Value("${info.app.version:1.0.0}")
    private String appVersion;

    @GetMapping
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", appName,
                "version", appVersion,
                "timestamp", OffsetDateTime.now().toString()
        );
    }
}

