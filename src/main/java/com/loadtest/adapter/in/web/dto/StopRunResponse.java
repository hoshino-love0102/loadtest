package com.loadtest.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

public record StopRunResponse(
        UUID runId,
        String status,
        Instant endedAt
) {}
