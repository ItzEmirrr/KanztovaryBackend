package kg.kanztovary.kanztovarybackend.domain.controller;

import jakarta.validation.Valid;
import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.domain.dto.user.ChangePasswordRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.user.UpdateProfileRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.user.UserProfileDto;
import kg.kanztovary.kanztovarybackend.domain.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileDto getProfile(@AuthenticationPrincipal User user) {
        return userService.getProfile(user);
    }

    @PutMapping("/me")
    public UserProfileDto updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateProfile(user, request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(user, request);
        return ResponseEntity.noContent().build();
    }
}