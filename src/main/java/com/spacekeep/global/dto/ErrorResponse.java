package com.spacekeep.global.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class ErrorResponse {

    private final boolean success = false;
    private final String code;
    private final String message;
    private final int status;
    private final String path;
    private final String method;
    private final String requestId;
    private final OffsetDateTime timestamp;
}
