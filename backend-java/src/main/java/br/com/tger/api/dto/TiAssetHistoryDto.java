package br.com.tger.api.dto;

import java.time.Instant;

public record TiAssetHistoryDto(
        Long id,
        Long assetId,
        String eventType,
        Long previousResponsibleUserId,
        String previousResponsibleUserName,
        Long newResponsibleUserId,
        String newResponsibleUserName,
        String previousStatus,
        String newStatus,
        Long previousTermId,
        String previousTermTitle,
        Long newTermId,
        String newTermTitle,
        String note,
        String changedByName,
        Instant changedAt
) {
}
