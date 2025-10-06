package com.loadtest.adapter.out.persistence;

import com.loadtest.application.port.out.TestDefinitionRepository;
import com.loadtest.domain.model.TestDefinition;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTestDefinitionRepository implements TestDefinitionRepository {

    private final Map<UUID, TestDefinition> store = new ConcurrentHashMap<>();

    @Override
    public UUID save(TestDefinition definition) {
        store.put(definition.id(), definition);
        return definition.id();
    }

    @Override
    public Optional<TestDefinition> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<TestDefinition> findAll() {
        return new ArrayList<>(store.values());
    }
}
