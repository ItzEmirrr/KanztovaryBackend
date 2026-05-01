package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.UserRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.user.ChangePasswordRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.user.UpdateProfileRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.user.UserProfileDto;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.service.UserService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserProfileDto getProfile(User user) {
        return toDto(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(User user, UpdateProfileRequest request) {
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseException(
                    ResponseStatus.EMAIL_EXISTS.getCode(),
                    ResponseStatus.EMAIL_EXISTS.getMessage()
            );
        }
        if (!user.getDisplayName().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseException(
                    ResponseStatus.USERNAME_EXISTS.getCode(),
                    ResponseStatus.USERNAME_EXISTS.getMessage()
            );
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        userRepository.save(user);
        log.info("Профиль обновлён: {}", user.getEmail());

        return toDto(user);
    }

    @Override
    @Transactional
    public void changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ResponseException(
                    ResponseStatus.WRONG_PASSWORD.getCode(),
                    ResponseStatus.WRONG_PASSWORD.getMessage()
            );
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseException(
                    ResponseStatus.PASSWORD_MISMATCH.getCode(),
                    ResponseStatus.PASSWORD_MISMATCH.getMessage()
            );
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Пароль изменён для пользователя: {}", user.getEmail());
    }

    private UserProfileDto toDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getDisplayName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}