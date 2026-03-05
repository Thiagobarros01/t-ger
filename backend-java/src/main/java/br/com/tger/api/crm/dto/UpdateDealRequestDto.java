package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.OpportunityType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateDealRequestDto(
        @NotNull Long clienteId,
        Long empresaId,
        @NotNull Long vendedorId,
        OpportunityType tipoOportunidade,
        BigDecimal valorEstimado,
        Integer probabilidade,
        LocalDate dataPrevistaFechamento
) {
}
