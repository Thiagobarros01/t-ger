package br.com.tger.api.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateStageRequestDto(
        @NotBlank String nome,
        @NotNull Integer ordem,
        boolean isWon,
        boolean isLost
) {
}

