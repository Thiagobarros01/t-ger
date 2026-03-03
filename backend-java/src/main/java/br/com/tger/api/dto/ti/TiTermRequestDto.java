package br.com.tger.api.dto.ti;

import br.com.tger.api.model.TermType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TiTermRequestDto(
        @NotNull TermType type,
        @NotBlank String linkedUserName,
        Long linkedAssetId,
        String linkedItemDescription,
        String startDate,
        @NotBlank String status,
        String documentPath
) {}
