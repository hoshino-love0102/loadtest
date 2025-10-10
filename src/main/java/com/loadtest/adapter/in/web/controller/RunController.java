package com.loadtest.adapter.in.web.controller;

import com.loadtest.adapter.in.web.dto.response.*;
import com.loadtest.application.port.in.RunUseCase;
import com.loadtest.domain.model.TestReport;
import com.loadtest.domain.model.TestRun;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class RunController {

    private final RunUseCase runUseCase;

    public RunController(RunUseCase runUseCase) {
        this.runUseCase = runUseCase;
    }

    @PostMapping("/tests/{testId}/runs")
    @ResponseStatus(HttpStatus.CREATED)
    public StartRunResponse start(@PathVariable UUID testId) {
        UUID runId = runUseCase.start(testId);
        return new StartRunResponse(runId);
    }

    @GetMapping("/runs/{runId}")
    public RunStatusResponse status(@PathVariable UUID runId) {
        TestRun run = runUseCase.getStatus(runId);
        TestReport snap = runUseCase.getReport(runId);

        return new RunStatusResponse(
                run.runId(),
                run.testId(),
                run.status().name(),
                run.startedAt(),
                run.endedAt(),
                toResponse(snap)
        );
    }

    @GetMapping("/runs/{runId}/report")
    public RunReportResponse report(@PathVariable UUID runId) {
        TestRun run = runUseCase.getStatus(runId);
        TestReport rep = runUseCase.getReport(runId);
        return new RunReportResponse(run.status().name(), toResponse(rep));
    }

    @PostMapping("/runs/{runId}/stop")
    public StopRunResponse stop(@PathVariable UUID runId) {
        runUseCase.stop(runId);
        TestRun run = runUseCase.getStatus(runId);
        return new StopRunResponse(run.runId(), run.status().name(), run.endedAt());
    }

    private TestReportResponse toResponse(TestReport r) {
        Map<String, Long> codes = r.statusCodeCounts().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        Map.Entry::getValue
                ));

        return new TestReportResponse(
                r.totalRequests(),
                r.successCount(),
                r.failCount(),
                r.avgLatencyMs(),
                r.minLatencyMs(),
                r.maxLatencyMs(),
                r.p50(),
                r.p95(),
                r.p99(),
                codes
        );
    }

    @GetMapping("/runs/{runId}/timeseries")
    public RunTimeSeriesResponse timeseries(@PathVariable UUID runId) {
        var samples = runUseCase.getTimeSeries(runId);

        var out = new java.util.ArrayList<RunSampleResponse>(samples.size());

        com.loadtest.domain.model.RunSample prev = null;
        for (var s : samples) {
            var rep = s.report();

            double rps = 0.0;
            if (prev != null) {
                long delta = rep.totalRequests() - prev.report().totalRequests();
                long dt = java.time.Duration.between(prev.at(), s.at()).toMillis();
                if (dt <= 0) dt = 1000;
                rps = (double) delta / ((double) dt / 1000.0);
            }

            double failRate = (rep.totalRequests() == 0)
                    ? 0.0
                    : (double) rep.failCount() / (double) rep.totalRequests();

            out.add(new RunSampleResponse(
                    s.at(),
                    rep.totalRequests(),
                    rep.successCount(),
                    rep.failCount(),
                    rep.avgLatencyMs(),
                    rep.p50(),
                    rep.p95(),
                    rep.p99(),
                    rps,
                    failRate
            ));
            prev = s;
        }
        return new RunTimeSeriesResponse(out);
    }
}
