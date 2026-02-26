package br.com.tger.api.dto.commercial;

public record BulkUpsertResultDto(int total, int created, int updated, int errors) {}
