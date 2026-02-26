package br.com.tger.api.dto.admin;

import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminUserCreateRequestDto(
        @NotBlank String name,
        @NotBlank @Email String email,
        String erpCode,
        @NotNull UserProfile profile,
        List<ModuleCode> modules
) {}
