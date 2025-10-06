package com.loadtest.application.port.in;

import com.loadtest.domain.model.TestDefinition;

import java.util.List;
import java.util.UUID;

public interface TestUseCase {
    UUID create(TestDefinition definition);
    List<TestDefinition> list();
}
