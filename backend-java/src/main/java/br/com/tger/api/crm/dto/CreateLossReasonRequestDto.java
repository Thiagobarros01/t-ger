package br.com.tger.api.crm.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLossReasonRequestDto(@NotBlank String descricao, boolean ativo) {}
