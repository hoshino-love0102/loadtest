package com.loadtest.domain.model;

import java.util.Map;

public record TestReport(
        long totalRequests,
        long successCount,
        long failCount,
        double avgLatencyMs,
        long minLatencyMs,
        long maxLatencyMs,
        long p50,
        long p95,
        long p99,
        Map<Integer, Long> statusCodeCounts
) {
    public static TestReport empty() {
        return new TestReport(
                0, 0, 0,
                0.0,
                0, 0,
                0, 0, 0,
                Map.of()
        );
    }
}
