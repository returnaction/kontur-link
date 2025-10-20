package com.nikita.linkservice.service;

import com.nikita.linkservice.exception.BadRequestException;
import com.nikita.linkservice.exception.ShortLinkNotFoundException;
import com.nikita.linkservice.mapper.LinkMapper;
import com.nikita.linkservice.model.dto.*;
import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.LinkRepository;
import com.nikita.linkservice.repository.projection.LinkStatsView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;

@Service
public class LinkService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TOKEN_LEN = 10;
    private static final SecureRandom RNG = new SecureRandom();
    private static final String REDIS_KEY_PREFIX_LINKS = "linksvc:shortlink:";

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final RedisTemplate<String, String> redisTemplate;

    public LinkService(LinkRepository linkRepository, LinkMapper linkMapper, RedisTemplate<String, String> redisTemplate) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.redisTemplate = redisTemplate;
    }

    @Transactional
    public ResponseEntity<LinkResponse> generate(LinkRequest request) {

        String original = request.getOriginal();
        if (original == null || original.isBlank())
            throw new BadRequestException("Поле 'original' не может быть пустым");

        Optional<LinkEntity> existing = linkRepository.findByOriginal(request.getOriginal());
        if (existing.isPresent()) {
            return ResponseEntity.ok(new LinkResponse("/l/" + existing.get().getLink()));
        }

        String token = generateToken();
        LinkEntity entity = LinkEntity.builder()
                .original(request.getOriginal())
                .link(token)
                .count(0L)
                .build();
        entity = linkRepository.save(entity);
        String redisKey = REDIS_KEY_PREFIX_LINKS + token;
        redisTemplate.opsForValue().set(redisKey, original, Duration.ofHours(1));
        return ResponseEntity.ok(new LinkResponse("/l/" + entity.getLink()));
    }

    private String generateToken() {
        StringBuilder sb = new StringBuilder(LinkService.TOKEN_LEN);
        for (int i = 0; i < LinkService.TOKEN_LEN; i++) {
            int index = RNG.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    @Transactional
    public ResponseEntity<Void> redirect(String shortLink) {
        String redisKey = REDIS_KEY_PREFIX_LINKS + shortLink;
        String original = redisTemplate.opsForValue().get(redisKey);
        if (original == null) {
            original = linkRepository.findOriginalByLink(shortLink)
                    .orElseThrow(() -> new ShortLinkNotFoundException(shortLink));

            redisTemplate.opsForValue().set(redisKey, original, Duration.ofHours(1));
            System.out.println("Взято из бд");
        }

        linkRepository.incrementLinkCount(shortLink);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(original)).build();
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Page<LinkDto>> getStats(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        size = Math.min(size, 100);

        PageRequest pageable = PageRequest.of(page, size);
        Page<LinkStatsView> pageView = linkRepository.findAllStats(pageable);
        Page<LinkDto> pageLinkDto = linkMapper.toDto(pageView);
        return ResponseEntity.ok(pageLinkDto);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<LinkDto> getStat(String shortLink) {
        LinkStatsView result = linkRepository.findStatsByLink(shortLink)
                .orElseThrow(() -> new ShortLinkNotFoundException("Короткая ссылка не найдена"));

        LinkDto linkDto = linkMapper.toDto(result);
        return new ResponseEntity<>(linkDto, HttpStatus.OK);
    }
}