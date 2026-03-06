package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.AuthResponse;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.LoginRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.RegisterRequest;
import kg.kanztovary.kanztovarybackend.domain.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Регистрация пользователя: {}", request.getEmail());
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Вход пользователя: {}", request.getEmail());
        return authService.login(request);
    }
}
