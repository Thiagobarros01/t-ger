package br.com.tger.api.dto.admin;

import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.UserProfile;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateUserPermissionsRequestDto(
        @NotNull UserProfile profile,
        List<ModuleCode> modules
) {}
