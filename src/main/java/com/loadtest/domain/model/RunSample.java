package com.loadtest.domain.model;

import java.time.Instant;
import java.util.UUID;

public record RunSample(
        UUID runId,
        Instant at,
        TestReport report
) {}
