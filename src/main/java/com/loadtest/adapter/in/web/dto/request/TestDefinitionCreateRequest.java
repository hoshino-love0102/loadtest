package com.loadtest.adapter.in.web.dto.request;

import com.loadtest.domain.model.TestDefinition;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TestDefinitionCreateRequest(
        String name,
        String method,
        String url,
        Map<String, Object> headers,
        Object body,
        int vus,
        int durationSec,
        Integer rampUpSec
) {
    public TestDefinition toDomain() {
        return new TestDefinition(
                null,
                name,
                method,
                url,
                headers,
                body,
                vus,
                durationSec,
                rampUpSec,
                null
        );
    }
}
