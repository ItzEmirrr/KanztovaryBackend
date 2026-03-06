package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.UserRepository;
import kg.kanztovary.kanztovarybackend.config.security.JwtService;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.AuthResponse;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.LoginRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.RegisterRequest;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.enums.Roles;
import kg.kanztovary.kanztovarybackend.domain.service.AuthService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseException(
                    ResponseStatus.EMAIL_EXISTS.getCode(),
                    ResponseStatus.EMAIL_EXISTS.getMessage()
            );
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseException(
                    ResponseStatus.USERNAME_EXISTS.getCode(),
                    ResponseStatus.USERNAME_EXISTS.getMessage()
            );
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Roles.USER)
                .build();

        userRepository.save(user);
        log.info("Зарегистрирован новый пользователь: {}", request.getEmail());

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            throw new ResponseException(
                    ResponseStatus.INVALID_CREDENTIALS.getCode(),
                    ResponseStatus.INVALID_CREDENTIALS.getMessage()
            );
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.USER_NOT_FOUND.getCode(),
                        ResponseStatus.USER_NOT_FOUND.getMessage()
                ));

        String token = jwtService.generateToken(user);
        log.info("Пользователь вошёл: {}", request.getEmail());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
