package com.spacekeep.global.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    private final boolean success = true;
    private final T data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder().build();
    }
}
