package br.com.tger.api.dto.common;

import jakarta.validation.constraints.NotBlank;

public record CodeNameRequestDto(
        @NotBlank String name,
        String erpCode
) {}
