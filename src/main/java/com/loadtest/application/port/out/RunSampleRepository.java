package com.loadtest.application.port.out;

import com.loadtest.domain.model.RunSample;

import java.util.List;
import java.util.UUID;

public interface RunSampleRepository {
    void append(RunSample sample);
    List<RunSample> findByRunId(UUID runId);
    void clear(UUID runId);
}
