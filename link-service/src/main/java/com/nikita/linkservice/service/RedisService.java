package com.nikita.linkservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisService {

    private static final String REDIS_KEY_PREFIX_LINKS = "linksvc:shortlink:";
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addCashLink(String shortLink, String original) {
        String redisKey = REDIS_KEY_PREFIX_LINKS + shortLink;
        redisTemplate.opsForValue().set(redisKey, original, Duration.ofHours(1));
    }

    public void removeCacheLink(String shortLink) {
        redisTemplate.delete(REDIS_KEY_PREFIX_LINKS + shortLink);
    }

    public String getCacheLink(String shortLink) {
        return redisTemplate.opsForValue().get(REDIS_KEY_PREFIX_LINKS + shortLink);
    }
}