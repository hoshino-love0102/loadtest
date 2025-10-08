package com.loadtest.adapter.out.persistence;

import com.loadtest.application.port.out.RunRuntimeStore;
import com.loadtest.domain.model.RunRuntime;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRunRuntimeStore implements RunRuntimeStore {

    private final ConcurrentHashMap<UUID, RunRuntime> store = new ConcurrentHashMap<>();

    @Override
    public void put(RunRuntime runtime) {
        store.put(runtime.runId(), runtime);
    }

    @Override
    public Optional<RunRuntime> get(UUID runId) {
        return Optional.ofNullable(store.get(runId));
    }

    @Override
    public void remove(UUID runId) {
        store.remove(runId);
    }
}
