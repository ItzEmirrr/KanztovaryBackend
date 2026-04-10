package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.User;
import kg.kanztovary.kanztovarybackend.domain.dto.user.ChangePasswordRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.user.UpdateProfileRequest;
import kg.kanztovary.kanztovarybackend.domain.dto.user.UserProfileDto;

public interface UserService {

    /** Получить профиль */
    UserProfileDto getProfile(User user);

    /** Обновление профиля */
    UserProfileDto updateProfile(User user, UpdateProfileRequest request);

    /** Смена пароля */
    void changePassword(User user, ChangePasswordRequest request);
}