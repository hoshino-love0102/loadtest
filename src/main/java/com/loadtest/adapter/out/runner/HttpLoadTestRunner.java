package com.loadtest.adapter.out.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loadtest.application.port.out.LoadTestRunner;
import com.loadtest.domain.model.RequestMetricEvent;
import com.loadtest.domain.model.RunRuntime;
import com.loadtest.domain.model.TestDefinition;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class HttpLoadTestRunner implements LoadTestRunner {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpLoadTestRunner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(3))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Override
    public void start(TestDefinition def, RunRuntime runtime, Runnable onFinish) {
        int vus = Math.max(1, def.vus());
        int rampUpSec = (def.rampUpSec() == null) ? 0 : Math.max(0, def.rampUpSec());

        CompletableFuture<?>[] futures = new CompletableFuture<?>[vus];

        for (int i = 0; i < vus; i++) {
            final int vuIndex = i;
            futures[i] = CompletableFuture.runAsync(() -> {
                rampUpDelayIfNeeded(vuIndex, vus, rampUpSec, runtime);
                runLoop(def, runtime);
            }, runtime.executor());
        }

        CompletableFuture.allOf(futures).whenComplete((v, ex) -> {
            try {
                onFinish.run();
            } catch (Exception ignore) {}
        });
    }

    private void runLoop(TestDefinition def, RunRuntime runtime) {
        while (!runtime.isStopped() && Instant.now().isBefore(runtime.deadline())) {
            try {
                HttpRequest req = buildRequest(def);
                long startNs = System.nanoTime();
                HttpResponse<byte[]> res = httpClient.send(req, HttpResponse.BodyHandlers.ofByteArray());
                long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

                runtime.aggregator().record(RequestMetricEvent.of(latencyMs, res.statusCode()));
            } catch (Exception e) {
                // 네트워크 오류/타임아웃/기타 예외는 statusCode=0으로 집계
                runtime.aggregator().record(new RequestMetricEvent(0, 0, true));
            }
        }
    }

    private void rampUpDelayIfNeeded(int vuIndex, int vus, int rampUpSec, RunRuntime runtime) {
        if (rampUpSec <= 0) return;

        long totalDelayMs = rampUpSec * 1000L;
        long delayMs = (vus <= 1) ? 0 : (totalDelayMs * vuIndex) / (vus - 1);

        if (delayMs <= 0) return;

        long end = System.currentTimeMillis() + delayMs;
        while (!runtime.isStopped() && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(Math.min(50, end - System.currentTimeMillis()));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private HttpRequest buildRequest(TestDefinition def) throws Exception {
        String method = Objects.requireNonNullElse(def.method(), "GET").toUpperCase();
        HttpRequest.Builder b = HttpRequest.newBuilder()
                .uri(URI.create(def.url()))
                .timeout(java.time.Duration.ofSeconds(10));

        if (def.headers() != null) {
            for (Map.Entry<String, Object> e : def.headers().entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    b.header(e.getKey(), String.valueOf(e.getValue()));
                }
            }
        }

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
        if (!("GET".equals(method) || "DELETE".equals(method))) {
            if (def.body() != null) {
                String json = (def.body() instanceof String s)
                        ? s
                        : objectMapper.writeValueAsString(def.body());
                bodyPublisher = HttpRequest.BodyPublishers.ofString(json);
                b.header("Content-Type", "application/json");
            }
        }

        return switch (method) {
            case "GET" -> b.GET().build();
            case "POST" -> b.POST(bodyPublisher).build();
            case "PUT" -> b.PUT(bodyPublisher).build();
            case "PATCH" -> b.method("PATCH", bodyPublisher).build();
            case "DELETE" -> b.DELETE().build();
            default -> b.method(method, bodyPublisher).build();
        };
    }
}
