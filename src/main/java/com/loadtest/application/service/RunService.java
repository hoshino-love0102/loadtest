package com.loadtest.application.service;

import com.loadtest.application.port.in.RunUseCase;
import com.loadtest.application.port.out.TestDefinitionRepository;
import com.loadtest.application.port.out.TestRunRepository;
import com.loadtest.domain.model.MetricsAggregator;
import com.loadtest.domain.model.TestDefinition;
import com.loadtest.domain.model.TestReport;
import com.loadtest.domain.model.TestRun;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RunService implements RunUseCase {

    private final TestDefinitionRepository testDefinitionRepository;
    private final TestRunRepository testRunRepository;

    private final Map<UUID, MetricsAggregator> aggregators = new ConcurrentHashMap<>();

    public RunService(TestDefinitionRepository testDefinitionRepository,
                      TestRunRepository testRunRepository) {
        this.testDefinitionRepository = testDefinitionRepository;
        this.testRunRepository = testRunRepository;
    }

    @Override
    public UUID start(UUID testId) {
        Optional<TestDefinition> testOpt = testDefinitionRepository.findById(testId);
        if (testOpt.isEmpty()) {
            throw new IllegalArgumentException("TestDefinition not found: " + testId);
        }

        UUID runId = UUID.randomUUID();
        TestRun run = new TestRun(
                runId,
                testId,
                TestRun.Status.RUNNING,
                Instant.now(),
                null
        );

        testRunRepository.save(run);
        aggregators.put(runId, new MetricsAggregator());

        return runId;
    }

    @Override
    public TestRun getStatus(UUID runId) {
        return testRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("TestRun not found: " + runId));
    }

    @Override
    public void stop(UUID runId) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("TestRun not found: " + runId));

        if (run.status() != TestRun.Status.RUNNING) {
            return; // idempotent stop
        }

        TestRun stopped = new TestRun(
                run.runId(),
                run.testId(),
                TestRun.Status.STOPPED,
                run.startedAt(),
                Instant.now()
        );

        testRunRepository.update(stopped);
    }

    @Override
    public TestReport getReport(UUID runId) {
        MetricsAggregator agg = aggregators.get(runId);
        if (agg == null) {
            return TestReport.empty();
        }
        return agg.snapshot();
    }
}