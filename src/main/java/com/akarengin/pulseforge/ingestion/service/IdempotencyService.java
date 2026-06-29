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
    private static final long TTL_SECONDS = 3600;
    private static final String PREFIX = "idempotency";

    public boolean checkAndSet(UUID workspaceId, String idempotencyKey) {
        String key = buildKey(workspaceId, idempotencyKey);
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofHours(TTL_SECONDS)));
    }

    public void delete(UUID workspaceId, String idempotencyKey) {
        String key = buildKey(workspaceId, idempotencyKey);
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException e) {
            log.warn("Failed to delete idempotency key: key={}, error={}", key, e.getMessage(), e);
            // fail-open: key may linger, but that's better than crashing during compensation
        }
    }

    private String buildKey(UUID workspaceId, String idempotencyKey) {
        return PREFIX + ":" + workspaceId + ":" + idempotencyKey;
    }
}
