package br.com.tger.api.crm.dto;

import jakarta.validation.constraints.NotNull;

public record CloseDealLostRequestDto(@NotNull Long motivoPerdaId) {}
