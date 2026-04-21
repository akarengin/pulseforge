package com.akarengin.pulseforge.mapper;

import com.akarengin.pulseforge.dto.UserResponse;
import com.akarengin.pulseforge.entity.User;
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
