package com.loadtest.application.service;

import com.loadtest.application.port.in.RunUseCase;
import com.loadtest.application.port.out.*;
import com.loadtest.domain.model.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;

public class RunService implements RunUseCase {

    private final TestDefinitionRepository testDefinitionRepository;
    private final TestRunRepository testRunRepository;
    private final RunRuntimeStore runtimeStore;
    private final LoadTestRunner loadTestRunner;
    private final TestReportRepository reportRepository;
    private final RunSampleRepository sampleRepository;
    private final ScheduledExecutorService sampler = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<UUID, ScheduledFuture<?>> samplingTasks = new ConcurrentHashMap<>();

    public RunService(TestDefinitionRepository testDefinitionRepository,
                      TestRunRepository testRunRepository,
                      RunRuntimeStore runtimeStore,
                      LoadTestRunner loadTestRunner,
                      TestReportRepository reportRepository,
                      RunSampleRepository sampleRepository) {
        this.testDefinitionRepository = testDefinitionRepository;
        this.testRunRepository = testRunRepository;
        this.runtimeStore = runtimeStore;
        this.loadTestRunner = loadTestRunner;
        this.reportRepository = reportRepository;
        this.sampleRepository = sampleRepository;
    }

    @Override
    public UUID start(UUID testId) {
        TestDefinition def = testDefinitionRepository.findById(testId)
                .orElseThrow(() -> new IllegalArgumentException("TestDefinition not found: " + testId));

        UUID runId = UUID.randomUUID();

        TestRun run = new TestRun(runId, testId, TestRun.Status.RUNNING, Instant.now(), null);
        testRunRepository.save(run);
        MetricsAggregator agg = new MetricsAggregator();
        Instant deadline = Instant.now().plusSeconds(Math.max(1, def.durationSec()));

        int threads = Math.max(1, def.vus());
        var executor = Executors.newFixedThreadPool(threads);
        RunRuntime runtime = new RunRuntime(runId, testId, deadline, agg, executor);
        runtimeStore.put(runtime);
        startSampling(runId);
        loadTestRunner.start(def, runtime, () -> finishRunDone(runId));

        return runId;
    }

    // 실행 중 메트릭을 1초마다 스냅샷으로 저장
    private void startSampling(UUID runId) {
        samplingTasks.computeIfAbsent(runId, id ->
                sampler.scheduleAtFixedRate(() -> {
                    var opt = runtimeStore.get(runId);
                    if (opt.isEmpty()) {
                        stopSampling(runId);
                        return;
                    }
                    var rt = opt.get();
                    TestReport rep = rt.aggregator().snapshot();
                    sampleRepository.append(new RunSample(runId, Instant.now(), rep));

                }, 0, 1, TimeUnit.SECONDS)
        );
    }

    // 샘플링 작업 중단
    private void stopSampling(UUID runId) {
        ScheduledFuture<?> f = samplingTasks.remove(runId);
        if (f != null) f.cancel(false);
    }

    // 테스트가 정상적으로 종료되었을 때
    private void finishRunDone(UUID runId) {
        Optional<TestRun> opt = testRunRepository.findById(runId);
        if (opt.isEmpty()) {
            stopSampling(runId);
            cleanupRuntime(runId);
            return;
        }

        TestRun cur = opt.get();

        // 이미 stopped 등으로 종료된 경우
        if (cur.status() != TestRun.Status.RUNNING) {
            persistFinalReportIfPresent(runId);
            stopSampling(runId);
            cleanupRuntime(runId);
            return;
        }

        // 최종 리포트 저장
        persistFinalReportIfPresent(runId);

        TestRun done = new TestRun(
                cur.runId(),
                cur.testId(),
                TestRun.Status.DONE,
                cur.startedAt(),
                Instant.now()
        );
        testRunRepository.update(done);

        stopSampling(runId);
        cleanupRuntime(runId);
    }

    // 최종 메트릭 리포트 저장
    private void persistFinalReportIfPresent(UUID runId) {
        runtimeStore.get(runId).ifPresent(rt -> {
            TestReport finalReport = rt.aggregator().snapshot();
            reportRepository.save(runId, finalReport);
        });
    }

    // 실행 중 런타임 정리
    private void cleanupRuntime(UUID runId) {
        runtimeStore.get(runId).ifPresent(rt -> {
            rt.stopNow();
            rt.executor().shutdownNow();
        });
        runtimeStore.remove(runId);
    }

    // 테스트 실행 상태 조회
    @Override
    public TestRun getStatus(UUID runId) {
        return testRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("TestRun not found: " + runId));
    }

    // 테스트 수동 중단
    @Override
    public void stop(UUID runId) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("TestRun not found: " + runId));

        if (run.status() != TestRun.Status.RUNNING) return;
        // 중단 직전 메트릭 저장 후 중단
        runtimeStore.get(runId).ifPresent(rt -> {
            reportRepository.save(runId, rt.aggregator().snapshot());
            rt.stopNow();
            rt.executor().shutdownNow();
        });

        // 멈춤 상태로 변경
        TestRun stopped = new TestRun(
                run.runId(),
                run.testId(),
                TestRun.Status.STOPPED,
                run.startedAt(),
                Instant.now()
        );
        testRunRepository.update(stopped);

        stopSampling(runId);
        runtimeStore.remove(runId);
    }

    // 테스트 리포트 조회
    @Override
    public TestReport getReport(UUID runId) {
        return runtimeStore.get(runId)
                .map(rt -> rt.aggregator().snapshot())
                .orElseGet(() ->
                        reportRepository.findByRunId(runId).orElse(TestReport.empty())
                );
    }

    // UI용 시계열 메트릭 조회
    @Override
    public List<RunSample> getTimeSeries(UUID runId) {
        return sampleRepository.findByRunId(runId);
    }
}
