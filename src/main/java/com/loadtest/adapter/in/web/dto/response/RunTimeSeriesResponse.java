package com.loadtest.adapter.in.web.dto.response;

import java.util.List;

public record RunTimeSeriesResponse(List<RunSampleResponse> samples) {}
