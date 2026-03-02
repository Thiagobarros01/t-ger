package br.com.tger.api.crm.dto;

import br.com.tger.api.crm.domain.BusinessType;

public record PipelineResponseDto(Long id, String nome, BusinessType tipoNegocio, boolean ativo) {}
