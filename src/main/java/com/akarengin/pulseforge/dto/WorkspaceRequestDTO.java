package com.akarengin.pulseforge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkspaceRequestDTO(
    @NotBlank(message = "Workspace name is required")
    @Size(max = 100)
    String name
) {

}