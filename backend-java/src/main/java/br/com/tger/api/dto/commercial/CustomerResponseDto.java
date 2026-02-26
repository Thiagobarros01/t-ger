package br.com.tger.api.dto.commercial;

public record CustomerResponseDto(
        Long id, String code, String erpCode, String corporateName, String email, String type, String tradeName, String phone, String erpSellerCode
) {}
