package br.com.tger.api.crm.dto;

import br.com.tger.api.dto.common.PagedResponseDto;

public record CrmCustomerProfileResponseDto(
        CrmCustomerProfileSummaryDto summary,
        PagedResponseDto<CrmCustomerOrderHistoryItemDto> history
) {}
