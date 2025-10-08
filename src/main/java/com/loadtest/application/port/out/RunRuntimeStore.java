package com.loadtest.application.port.out;

import com.loadtest.domain.model.RunRuntime;

import java.util.Optional;
import java.util.UUID;

public interface RunRuntimeStore {
    void put(RunRuntime runtime);
    Optional<RunRuntime> get(UUID runId);
    void remove(UUID runId);
}
