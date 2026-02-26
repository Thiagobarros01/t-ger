package br.com.tger.api.dto;

import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.UserProfile;

import java.util.List;

public record UserDto(
        Long id,
        String name,
        String email,
        UserProfile profile,
        List<ModuleCode> modules
) {
}
