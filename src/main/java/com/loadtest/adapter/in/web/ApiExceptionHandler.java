package com.loadtest.adapter.in.web;

import com.loadtest.adapter.in.web.dto.response.TargetRejectedResponse;
import com.loadtest.application.service.TargetRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TargetRejectedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public TargetRejectedResponse handleTargetRejected(TargetRejectedException e) {
        return new TargetRejectedResponse(
                "TARGET_REJECTED",
                e.reason().name(),
                e.detail()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public TargetRejectedResponse handleBadRequest(IllegalArgumentException e) {
        return new TargetRejectedResponse(
                "BAD_REQUEST",
                "INVALID_ARGUMENT",
                e.getMessage()
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public TargetRejectedResponse handleUnknown(Exception e) {
        return new TargetRejectedResponse(
                "INTERNAL_ERROR",
                "UNEXPECTED",
                "unexpected error"
        );
    }
}
