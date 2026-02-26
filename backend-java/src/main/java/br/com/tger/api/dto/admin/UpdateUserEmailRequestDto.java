package br.com.tger.api.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserEmailRequestDto(@NotBlank @Email String email) {}
