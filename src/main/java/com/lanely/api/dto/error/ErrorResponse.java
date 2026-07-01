package com.lanely.api.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(name = "ErrorResponse", description = "Standard error payload returned for every non-2xx response")
public record ErrorResponse(

        @Schema(description = "Instant the error was produced (ISO-8601 UTC)", example = "2026-06-10T12:34:56.789Z")
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "HTTP status reason phrase", example = "Bad Request")
        String error,

        @Schema(description = "Human-readable description of the error", example = "Validation failed for one or more fields")
        String message,

        @Schema(description = "Request path that produced the error", example = "/auth/register")
        String path,

        @Schema(description = "Field-level validation errors, present only for 400 validation failures", nullable = true)
        List<FieldErrorDto> fieldErrors
) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<FieldErrorDto> fieldErrors) {
        return new ErrorResponse(Instant.now(), status, error, message, path, fieldErrors);
    }
}
