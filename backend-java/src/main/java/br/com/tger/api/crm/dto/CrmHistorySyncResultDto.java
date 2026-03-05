package br.com.tger.api.crm.dto;

public record CrmHistorySyncResultDto(
        int analyzed,
        int created,
        int updated,
        int skipped,
        int unmapped
) {}
