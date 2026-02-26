package br.com.tger.api.dto;

import java.util.List;

public record TicketDto(
        Long id,
        String subject,
        String requester,
        String assignedTo,
        String priority,
        String status,
        List<TicketMessageDto> messages
) {
}
