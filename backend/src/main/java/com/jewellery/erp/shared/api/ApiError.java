package com.jewellery.erp.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        String code,
        String message,
        String traceId,
        Instant timestamp,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {
    }

    public static ApiError of(String code, String message, String traceId) {
        return new ApiError(code, message, traceId, Instant.now(), List.of());
    }

    public static ApiError of(String code, String message, String traceId, List<FieldError> fieldErrors) {
        return new ApiError(code, message, traceId, Instant.now(), fieldErrors);
    }
}
