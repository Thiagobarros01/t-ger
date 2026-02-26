package br.com.tger.api.dto.commercial;

import jakarta.validation.constraints.NotBlank;

public record ProductRequestDto(
        String erpCode,
        @NotBlank String description,
        String department,
        String category,
        String line,
        String manufacturer
) {}
