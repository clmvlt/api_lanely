package com.lanely.api.dto.error;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "FieldError", description = "Validation error for a single request field")
public record FieldErrorDto(

        @Schema(description = "Name of the rejected field", example = "email")
        String field,

        @Schema(description = "Human-readable reason the field was rejected", example = "must be a well-formed email address")
        String message
) {
}
