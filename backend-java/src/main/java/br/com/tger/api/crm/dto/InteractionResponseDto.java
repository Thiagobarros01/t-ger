package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.InteractionType;

import java.time.Instant;

public record InteractionResponseDto(
        Long id,
        Long clienteId,
        Long dealId,
        InteractionType tipo,
        String descricao,
        Instant ocorridoEm,
        Long criadoPor,
        Instant createdAt
) {}
