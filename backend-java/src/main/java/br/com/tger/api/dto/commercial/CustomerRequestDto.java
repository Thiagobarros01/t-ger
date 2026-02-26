package br.com.tger.api.dto.commercial;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequestDto(
        String erpCode,
        @NotBlank String corporateName,
        String email,
        @NotBlank String type,
        String tradeName,
        String phone,
        String erpSellerCode
) {}
