package br.com.tger.api.dto;

import br.com.tger.api.model.AssetStatus;
import br.com.tger.api.model.AssetType;
import br.com.tger.api.model.IpMode;

import java.util.List;
import java.util.Map;

public record TiAssetDto(
        Long id,
        String internalCode,
        String company,
        String companyErpCode,
        AssetType assetType,
        String department,
        String brand,
        String model,
        String serialNumber,
        String patrimony,
        String detailedDescription,
        AssetStatus status,
        Long responsibleUserId,
        String responsibleUserName,
        Long linkedTermId,
        String linkedTermTitle,
        List<String> transferHistory,
        IpMode ipMode,
        String ipAddress,
        String imei,
        Map<String, String> extraFields
) {
}
