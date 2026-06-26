package com.akarengin.pulseforge.ingestion.service;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean checkAndSet(UUID workspaceId, String key) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent("idemp:" + workspaceId + ":" + key, "1", Duration.ofHours(1)));
    }
}
