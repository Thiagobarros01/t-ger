package br.com.tger.api.dto.ti;

import br.com.tger.api.model.AssetStatus;
import br.com.tger.api.model.AssetType;
import br.com.tger.api.model.IpMode;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record TiAssetRequestDto(
        String company,
        String companyErpCode,
        @NotNull AssetType assetType,
        String department,
        String brand,
        String model,
        String serialNumber,
        String patrimony,
        String detailedDescription,
        @NotNull AssetStatus status,
        Long responsibleUserId,
        String responsibleUserName,
        Long linkedTermId,
        String linkedTermTitle,
        List<String> transferHistory,
        @NotNull IpMode ipMode,
        String ipAddress,
        String imei,
        Map<String, String> extraFields
) {}
