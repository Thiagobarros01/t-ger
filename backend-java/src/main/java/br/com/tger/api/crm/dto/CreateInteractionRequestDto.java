package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.InteractionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateInteractionRequestDto(
        @NotNull Long clienteId,
        Long dealId,
        @NotNull InteractionType tipo,
        @NotBlank String descricao,
        @NotNull Instant ocorridoEm,
        @NotNull Long criadoPor
) {}
