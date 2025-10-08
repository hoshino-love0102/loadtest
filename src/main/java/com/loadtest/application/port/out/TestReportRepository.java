package com.loadtest.application.port.out;

import com.loadtest.domain.model.TestReport;

import java.util.Optional;
import java.util.UUID;

public interface TestReportRepository {
    void save(UUID runId, TestReport report);
    Optional<TestReport> findByRunId(UUID runId);
}
