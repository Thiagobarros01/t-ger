package br.com.tger.api.dto.common;

import java.util.List;

public record PagedResponseDto<T>(
        List<T> items,
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
}
