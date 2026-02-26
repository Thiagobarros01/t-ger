package br.com.tger.api.dto.ti;

import jakarta.validation.constraints.NotBlank;

public record TicketStatusUpdateRequestDto(@NotBlank String status) {}
