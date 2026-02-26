package br.com.tger.api.dto.commercial;

import jakarta.validation.constraints.NotBlank;

public record SellerRequestDto(
        @NotBlank String erpCode,
        @NotBlank String name,
        String email,
        String phone
) {}
