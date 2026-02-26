package br.com.tger.api.dto.admin;

import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.UserProfile;

import java.time.Instant;
import java.util.List;

public record AdminUserResponseDto(
        Long id,
        String name,
        String email,
        String erpCode,
        UserProfile profile,
        boolean active,
        Instant lastPasswordResetAt,
        List<ModuleCode> modules
) {}
