package br.com.tger.api.dto.commercial;

public record ProductResponseDto(
        Long id, String code, String erpCode, String description, String department, String category, String line, String manufacturer
) {}
