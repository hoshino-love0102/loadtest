package com.loadtest.adapter.out.persistence;

import com.loadtest.application.port.out.RunSampleRepository;
import com.loadtest.domain.model.RunSample;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRunSampleRepository implements RunSampleRepository {

    private static final int MAX_POINTS_PER_RUN = 600;
    private final Map<UUID, Deque<RunSample>> store = new ConcurrentHashMap<>();

    @Override
    public void append(RunSample sample) {
        Deque<RunSample> q = store.computeIfAbsent(sample.runId(), k -> new ArrayDeque<>());
        synchronized (q) {
            q.addLast(sample);
            while (q.size() > MAX_POINTS_PER_RUN) {
                q.removeFirst();
            }
        }
    }

    @Override
    public List<RunSample> findByRunId(UUID runId) {
        Deque<RunSample> q = store.get(runId);
        if (q == null) return List.of();
        synchronized (q) {
            return new ArrayList<>(q);
        }
    }

    @Override
    public void clear(UUID runId) {
        store.remove(runId);
    }
}
