package com.loadtest.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TestRun(
        UUID runId,
        UUID testId,
        Status status,
        Instant startedAt,
        Instant endedAt
) {
    public enum Status {
        RUNNING, STOPPED, DONE
    }
}
