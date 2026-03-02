package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.TaskPriority;
import br.com.tger.api.crm.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateTaskRequestDto(
        @NotNull Long clienteId,
        Long dealId,
        @NotBlank String titulo,
        String descricao,
        @NotNull TaskPriority prioridade,
        @NotNull TaskStatus status,
        Instant vencimentoEm,
        @NotNull Long responsavelId
) {}
