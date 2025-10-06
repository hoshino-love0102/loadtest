package com.loadtest.adapter.in.web.dto;

public record RunReportResponse(
        String state,
        TestReportResponse report
) {}
