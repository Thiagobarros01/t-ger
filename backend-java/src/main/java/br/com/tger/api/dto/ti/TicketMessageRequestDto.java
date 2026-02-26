package br.com.tger.api.dto.ti;

import jakarta.validation.constraints.NotBlank;

public record TicketMessageRequestDto(
        @NotBlank String author,
        String sentAt,
        @NotBlank String message
) {}
