package br.com.tger.api.dto.common;

public record GlobalParameterDto(
        String key,
        String label,
        String description,
        String value
) {
}

