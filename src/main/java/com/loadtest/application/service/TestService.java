package com.loadtest.application.service;

import com.loadtest.application.port.in.TestUseCase;
import com.loadtest.application.port.out.TestDefinitionRepository;
import com.loadtest.domain.model.TestDefinition;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TestService implements TestUseCase {

    private final TestDefinitionRepository testDefinitionRepository;

    public TestService(TestDefinitionRepository testDefinitionRepository) {
        this.testDefinitionRepository = testDefinitionRepository;
    }

    @Override
    public UUID create(TestDefinition testDefinition) {
        UUID id = (testDefinition.id() == null) ? UUID.randomUUID() : testDefinition.id();
        Instant createdAt = (testDefinition.createdAt() == null) ? Instant.now() : testDefinition.createdAt();

        TestDefinition normalized = new TestDefinition(
                id,
                testDefinition.name(),
                testDefinition.method(),
                testDefinition.url(),
                testDefinition.headers(),
                testDefinition.body(),
                testDefinition.vus(),
                testDefinition.durationSec(),
                testDefinition.rampUpSec(),
                createdAt
        );

        testDefinitionRepository.save(normalized);
        return id;
    }

    @Override
    public List<TestDefinition> list() {
        return testDefinitionRepository.findAll();
    }
}
