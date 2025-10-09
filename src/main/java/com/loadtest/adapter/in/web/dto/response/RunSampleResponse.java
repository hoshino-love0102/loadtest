package com.loadtest.adapter.in.web.dto.response;

import java.time.Instant;

public record RunSampleResponse(
        Instant at,
        long totalRequests,
        long successCount,
        long failCount,
        double avgLatencyMs,
        long p50,
        long p95,
        long p99,
        double rps,
        double failRate
) {}
