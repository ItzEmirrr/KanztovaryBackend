package kg.kanztovary.kanztovarybackend.domain.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileDto {
    private Integer id;
    private String username;
    private String email;
    private String role;
}