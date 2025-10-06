package com.loadtest.adapter.out.persistence;

import com.loadtest.application.port.out.TestRunRepository;
import com.loadtest.domain.model.TestRun;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTestRunRepository implements TestRunRepository {

    private final Map<UUID, TestRun> store = new ConcurrentHashMap<>();

    @Override
    public void save(TestRun run) {
        store.put(run.runId(), run);
    }

    @Override
    public Optional<TestRun> findById(UUID runId) {
        return Optional.ofNullable(store.get(runId));
    }

    @Override
    public void update(TestRun run) {
        store.put(run.runId(), run);
    }
}
