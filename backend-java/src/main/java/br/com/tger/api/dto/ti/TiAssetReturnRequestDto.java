package br.com.tger.api.dto.ti;

import br.com.tger.api.model.EquipmentCondition;
import jakarta.validation.constraints.NotNull;

public record TiAssetReturnRequestDto(
        @NotNull EquipmentCondition equipmentCondition
) {}

