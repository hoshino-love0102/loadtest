package com.loadtest.application.port.out;

import com.loadtest.domain.model.RunRuntime;
import com.loadtest.domain.model.TestDefinition;

public interface LoadTestRunner {
    void start(TestDefinition def, RunRuntime runtime, Runnable onFinish);
}
