package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePipelineRequestDto(
        @NotBlank String nome,
        @NotNull BusinessType tipoNegocio,
        boolean ativo
) {}
