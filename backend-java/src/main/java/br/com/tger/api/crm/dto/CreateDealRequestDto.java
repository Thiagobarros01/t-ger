package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.BusinessType;
import br.com.tger.api.crm.domain.OpportunityType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateDealRequestDto(
        @NotNull Long clienteId,
        Long empresaId,
        @NotNull Long vendedorId,
        @NotNull BusinessType tipoNegocio,
        OpportunityType tipoOportunidade,
        @NotNull Long pipelineId,
        @NotNull Long stageId,
        BigDecimal valorEstimado,
        Integer probabilidade,
        LocalDate dataPrevistaFechamento,
        Long motivoPerdaId
) {}
