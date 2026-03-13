package com.akarengin.pulseforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record EventRequest(
    @NotBlank String type,
    @NotNull Map<String, Object> payload
) {

}