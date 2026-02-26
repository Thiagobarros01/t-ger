package br.com.tger.api.dto;

public record LoginResponseDto(
        String token,
        UserDto user
) {
}
