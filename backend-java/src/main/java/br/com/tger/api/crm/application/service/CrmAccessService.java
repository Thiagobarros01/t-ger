package br.com.tger.api.crm.application.service;

import br.com.tger.api.dto.UserDto;
import br.com.tger.api.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class CrmAccessService {
    private final AuthService authService;

    public CrmAccessService(AuthService authService) {
        this.authService = authService;
    }

    public UserDto resolveUser(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) return null;
        try {
            return authService.me(authorizationHeader);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean isOperator(UserDto user) {
        return user != null && user.profile() != null && "OPERADOR".equals(user.profile().name());
    }
}
