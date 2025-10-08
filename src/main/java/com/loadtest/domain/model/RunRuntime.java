package com.loadtest.domain.model;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class RunRuntime {

    private final UUID runId;
    private final UUID testId;
    private final Instant deadline;
    private final MetricsAggregator aggregator;
    private final ExecutorService executor;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public RunRuntime(UUID runId,
                      UUID testId,
                      Instant deadline,
                      MetricsAggregator aggregator,
                      ExecutorService executor) {
        this.runId = runId;
        this.testId = testId;
        this.deadline = deadline;
        this.aggregator = aggregator;
        this.executor = executor;
    }

    public UUID runId() { return runId; }
    public UUID testId() { return testId; }
    public Instant deadline() { return deadline; }
    public MetricsAggregator aggregator() { return aggregator; }
    public ExecutorService executor() { return executor; }

    public boolean isStopped() { return stop.get(); }
    public void stopNow() { stop.set(true); }
}
