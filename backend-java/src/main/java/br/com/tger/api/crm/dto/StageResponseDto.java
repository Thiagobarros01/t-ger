package br.com.tger.api.crm.dto;

public record StageResponseDto(Long id, Long pipelineId, String nome, Integer ordem, boolean isWon, boolean isLost) {}
