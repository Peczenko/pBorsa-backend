package com.pborsa.api.user;

import com.pborsa.domain.dto.user.UserProfileDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserProfileDto toProfile(UserEntity user) {
        if (user == null) {
            return null;
        }
        return new UserProfileDto(
                user.getId(),
                user.getFirebaseUid(),
                user.getEmail(),
                user.getDisplayName(),
                user.getProvider(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
