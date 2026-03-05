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
import java.util.List;

@Service
public class AuthService {

    private final SampleDataService sampleDataService;
    private final AppUserRepository appUserRepository;

    public AuthService(SampleDataService sampleDataService, AppUserRepository appUserRepository) {
        this.sampleDataService = sampleDataService;
        this.appUserRepository = appUserRepository;
    }

    public LoginResponseDto login(LoginRequestDto request) {
        UserDto user = appUserRepository.findByEmailIgnoreCase(request.email())
                .filter(entity -> entity.isActive())
                .map(entity -> new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getLinkedSellerErpCode(), entity.getProfile(), entity.getModules()))
                .orElseGet(() -> sampleDataService.getUsers().stream()
                        .filter(item -> item.email().equalsIgnoreCase(request.email()))
                        .findFirst()
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas")));

        if (!isValidPassword(user.email(), request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais invalidas");
        }

        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((user.email() + ":" + System.currentTimeMillis()).getBytes(StandardCharsets.UTF_8));
        return new LoginResponseDto(token, user);
    }

    public UserDto me(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        String email = extractEmailFromToken(token);
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        }
        return appUserRepository.findByEmailIgnoreCase(email)
                .filter(entity -> entity.isActive())
                .map(entity -> new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getLinkedSellerErpCode(), entity.getProfile(), entity.getModules()))
                .orElseGet(() -> resolveSampleUser(email));
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

    private String extractEmailFromToken(String token) {
        String decoded = tryDecodeToken(token);
        if (decoded == null) return null;
        List<String> parts = List.of(decoded.split(":"));
        if (parts.isEmpty()) return null;
        if (parts.size() >= 2) {
            if (parts.get(0).contains("@")) return parts.get(0); // formato novo: email:timestamp
            if (parts.get(1).contains("@")) return parts.get(1); // formato antigo: id:email
        }
        return parts.get(0).contains("@") ? parts.get(0) : null;
    }

    private String tryDecodeToken(String token) {
        try {
            return new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (Exception ignored) { }
        try {
            return new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (Exception ignored) { }
        return null;
    }

    private UserDto resolveSampleUser(String email) {
        return sampleDataService.getUsers().stream()
                .filter(item -> item.email().equalsIgnoreCase(email))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida"));
    }
}
