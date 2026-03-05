package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.BusinessType;
import br.com.tger.api.crm.domain.DealStatus;
import br.com.tger.api.crm.domain.OpportunityType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record DealResponseDto(
        Long id,
        Long clienteId,
        Long empresaId,
        Long vendedorId,
        BusinessType tipoNegocio,
        OpportunityType tipoOportunidade,
        Long pipelineId,
        Long stageId,
        BigDecimal valorEstimado,
        Integer probabilidade,
        DealStatus status,
        String numeroPedido,
        String situacaoPedido,
        LocalDate dataUltimoPedido,
        LocalDate dataPrevistaFechamento,
        Long motivoPerdaId,
        Instant createdAt,
        Instant updatedAt
) {}
