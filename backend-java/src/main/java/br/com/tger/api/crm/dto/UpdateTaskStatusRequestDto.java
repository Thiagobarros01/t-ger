package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequestDto(
        @NotNull TaskStatus status
) {
}

