package br.com.tger.api.controller;

import br.com.tger.api.dto.LoginRequestDto;
import br.com.tger.api.dto.LoginResponseDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@Valid @RequestBody LoginRequestDto request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return authService.me(authorizationHeader);
    }
}
