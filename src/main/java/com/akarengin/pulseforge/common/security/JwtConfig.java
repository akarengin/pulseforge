package com.akarengin.pulseforge.common.security;

import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    @Bean
    public SecretKey jwtSecretKey(JwtProperties jwtProperties) {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
