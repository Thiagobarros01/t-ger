package br.com.tger.api.crm.dto;

import jakarta.validation.constraints.NotNull;

public record MoveDealStageRequestDto(@NotNull Long stageId, Long motivoPerdaId) {}
