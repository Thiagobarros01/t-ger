package br.com.tger.api.dto;

public record TiAssetSummaryDto(
        long total,
        long available,
        long inUse,
        long returned
) {
}

