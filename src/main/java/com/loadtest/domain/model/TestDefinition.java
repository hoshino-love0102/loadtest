package com.loadtest.domain.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TestDefinition(
        UUID id,
        String name,
        String method,
        String url,
        Map<String, Object> headers,
        Object body,
        int vus,
        int durationSec,
        Integer rampUpSec,
        Instant createdAt
) {}
