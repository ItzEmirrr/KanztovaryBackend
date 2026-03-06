package kg.kanztovary.kanztovarybackend.domain.service;


import kg.kanztovary.kanztovarybackend.domain.dto.auth.AuthResponse;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.LoginRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
