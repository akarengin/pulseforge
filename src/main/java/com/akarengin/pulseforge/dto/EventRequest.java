package com.akarengin.pulseforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record EventRequest(
    @NotBlank(message = "Event type is required")
    String type,

    @NotNull
    @NotEmpty(message = "Event payload cannot be empty")
    @Size(max = 100, message = "Payload cannot have more than 100 entries")
    Map<String, Object> payload
) {

}
