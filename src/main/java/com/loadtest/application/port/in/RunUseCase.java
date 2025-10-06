package com.loadtest.application.port.in;

import com.loadtest.domain.model.TestRun;
import com.loadtest.domain.model.TestReport;

import java.util.UUID;

public interface RunUseCase {
    UUID start(UUID testId);
    TestRun getStatus(UUID runId);
    void stop(UUID runId);
    TestReport getReport(UUID runId);
}
