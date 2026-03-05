package br.com.tger.api.dto.commercial;

import java.util.List;

public record BulkUpsertResultDto(
        int total,
        int created,
        int updated,
        int errors,
        List<BulkUpsertErrorDto> errorDetails
) {}
