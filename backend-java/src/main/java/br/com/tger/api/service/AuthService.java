package br.com.tger.api.service;

import br.com.tger.api.dto.LoginRequestDto;
import br.com.tger.api.dto.LoginResponseDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final SampleDataService sampleDataService;
    private final AppUserRepository appUserRepository;
    private final Map<String, UserDto> tokenStore = new ConcurrentHashMap<>();

    public AuthService(SampleDataService sampleDataService, AppUserRepository appUserRepository) {
        this.sampleDataService = sampleDataService;
        this.appUserRepository = appUserRepository;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        UserDto user = appUserRepository.findByEmailIgnoreCase(request.email())
                .filter(entity -> entity.isActive())
                .map(entity -> new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getProfile(), entity.getModules()))
                .orElseGet(() -> sampleDataService.getUsers().stream()
                        .filter(item -> item.email().equalsIgnoreCase(request.email()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas")));

        if (!isValidPassword(user.email(), request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        String token = Base64.getEncoder()
                .encodeToString((user.id() + ":" + user.email()).getBytes(StandardCharsets.UTF_8));
        tokenStore.put(token, user);
        return new LoginResponseDto(token, user);
    }

    public UserDto me(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        UserDto user = tokenStore.get(token);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        }
        return user;
    }

    private boolean isValidPassword(String email, String password) {
        // Login simples para homologacao local (trocar por hash+JWT na proxima etapa)
        return "123456".equals(password) || ("admin@123".equals(password) && "thiago@tger.local".equalsIgnoreCase(email));
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token ausente");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }
}
