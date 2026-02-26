package br.com.tger.api.dto;

import br.com.tger.api.model.TermType;

public record TiTermDto(
        Long id,
        TermType type,
        String defaultTermName,
        String linkedUserName,
        String startDate,
        String status,
        String documentPath
) {
}
