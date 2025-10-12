# Safe, API-Driven HTTP Load Testing Service

This project provides a **server-based load testing platform** driven entirely through **REST APIs**.  
There is **no CLI or UI required**—define tests, execute them, and inspect runtime metrics through HTTP calls.

---

## Key Idea

Unlike tools like **JMeter** or **k6**, this platform is:

- **Server-hosted**, not a local executable
- **Controlled through APIs only**
- **Designed to prevent accidental or malicious DDoS**
- **Focused on real-time execution visibility (`vitals`)**

In short:

> A load-testing service that *cannot* be used to attack arbitrary targets—by mistake or on purpose.

---

## How It Works

### 1. Create a Test
Call `/tests` with:
- Target URL
- Number of virtual users (VU)
- Duration

### 2. Run the Test
Call `/tests/{testId}/runs`

### 3. Safety Gate (Before Execution Starts)

Runs are **blocked completely** if:
- URL scheme is not `http` / `https`
- Target resolves to private/internal IPs
- Host is not in the allowlist

If any check fails, **the run does not start**.

### 4. Execution
Once validated:
- A runtime instance is created
- VU thread pool fires repeated HTTP requests
- Results flow into an internal metrics aggregator

### 5. Real-Time Metrics (`Vitals`)
Every second, a snapshot captures:
- Request count
- Success/failure volume
- Avg / p50 / p95 / p99 latency
- RPS
- Failure rate

These are stored as **time-series samples** and can be queried via:
/runs/{id}/timeseries


This enables analysis such as:

- When latency degradation starts
- When error rates spike
- How behavior changes as load increases

### 6. Completion
When done (or manually stopped):
- Run state switches to `DONE` or `STOPPED`
- Final summary is persisted
- Runtime and thread pools are cleaned up

---

## Why Vitals Matter

Typical tools only tell you:
> “15 seconds of load produced N requests.”

This system answers:
- When latency blew up
- When failures started
- What load level triggered issues

Metrics are structured for dashboarding and live monitoring.

---

## Constant Memory by Design

Most load generators store **every request's latency**, causing:
- Memory growth proportional to duration
- Possible crashes on long tests

This system keeps memory **O(1)** by:
- Storing only histograms + counters
- Discarding individual request timings
- Computing percentiles from bucket data

So whether a test runs 10 seconds or 10 hours, memory remains stable.

---

## Strict Security

The service can send traffic out to the internet—so it must be safe.

Security rules:
- Allow **http/https only**
- Reject **private/internal networks**
    - `10.x.x.x`, `192.168.x.x`, `172.16–31.x.x`
- Reject **localhost** unless explicitly allowed
- Enforce **explicit allowlist**
- Block invalid targets *before* runtime starts

This prevents the system being repurposed into:
- A DDoS launcher
- A network scanner
- An internal reconnaissance tool

---

## Architecture

Built using **Hexagonal Architecture** so that:
- Domain remains pure Java
- Storage can swap (in-memory → DB)
- The runner can evolve (local → distributed/gRPC)
- Optional UI can be added later

Today it’s a minimal viable product,  
but the structure supports long-term growth.

---

## TL;DR

**A safe-by-design, real-time insights–focused, server-hosted HTTP load-testing MVP**  
driven entirely through REST APIs, with strict network protections and constant memory usage.
