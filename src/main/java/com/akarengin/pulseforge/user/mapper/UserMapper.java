package com.akarengin.pulseforge.user.mapper;

import com.akarengin.pulseforge.user.dto.UserResponse;
import com.akarengin.pulseforge.common.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}
