package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.BusinessType;
import br.com.tger.api.crm.domain.DealStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DealResponseDto(
        Long id,
        Long clienteId,
        Long empresaId,
        Long vendedorId,
        BusinessType tipoNegocio,
        Long pipelineId,
        Long stageId,
        BigDecimal valorEstimado,
        Integer probabilidade,
        DealStatus status,
        LocalDate dataPrevistaFechamento,
        Long motivoPerdaId,
        Instant createdAt,
        Instant updatedAt
) {}
