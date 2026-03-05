package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.TaskStatus;

import java.time.Instant;

public record TaskStatusHistoryResponseDto(
        Long id,
        Long taskId,
        TaskStatus previousStatus,
        TaskStatus newStatus,
        Long changedByUserId,
        String changedByName,
        Instant changedAt,
        String note
) {
}

