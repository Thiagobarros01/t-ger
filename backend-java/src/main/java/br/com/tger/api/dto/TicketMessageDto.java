package br.com.tger.api.dto;

public record TicketMessageDto(
        Long id,
        String author,
        String sentAt,
        String message,
        boolean fromCurrentUser
) {
}
