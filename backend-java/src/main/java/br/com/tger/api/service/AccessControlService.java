package br.com.tger.api.service;

import br.com.tger.api.dto.UserDto;
import br.com.tger.api.model.UserProfile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccessControlService {
    private final AuthService authService;

    public AccessControlService(AuthService authService) {
        this.authService = authService;
    }

    public UserDto resolveUser(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        try {
            return authService.me(authorizationHeader);
        } catch (Exception ex) {
            return null;
        }
    }

    public UserDto requireUser(String authorizationHeader) {
        UserDto user = resolveUser(authorizationHeader);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        }
        return user;
    }

    public boolean isOperator(UserDto user) {
        return user != null && user.profile() == UserProfile.OPERADOR;
    }

    public void assertNotOperator(UserDto user) {
        if (isOperator(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissao para essa operacao");
        }
    }
}
