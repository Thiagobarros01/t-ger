package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.TaskPriority;
import br.com.tger.api.crm.domain.TaskStatus;

import java.time.Instant;

public record TaskResponseDto(
        Long id,
        Long clienteId,
        Long dealId,
        String titulo,
        String descricao,
        TaskPriority prioridade,
        TaskStatus status,
        Instant vencimentoEm,
        Long responsavelId,
        Instant createdAt
) {}
