package com.nikita.linkservice.service;

import com.nikita.linkservice.mapper.LinkMapper;
import com.nikita.linkservice.model.dto.*;
import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.LinkRepository;
import com.nikita.linkservice.repository.projection.LinkStatsView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.security.SecureRandom;

@Service
public class LinkService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TOKEN_LEN = 10;
    private static final int MAX_RETRIES = 5;
    private static final SecureRandom RNG = new SecureRandom();

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;

    public LinkService(LinkRepository linkRepository, LinkMapper linkMapper) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
    }

    @Transactional
    public ResponseEntity<LinkResponse> generate(LinkRequest request) {
        String token = generateToken(TOKEN_LEN);
        LinkEntity entity = LinkEntity.builder()
                .original(request.getOriginal())
                .link(token)
                .count(0L)
                .build();
        entity = linkRepository.save(entity);
        return ResponseEntity.ok(new LinkResponse("/l/" + entity.getLink()));
    }

    private String generateToken(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RNG.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    @Transactional
    public ResponseEntity<Void> redirect(String shortLink) {
        String original = linkRepository.incrementAndGetOriginal(shortLink)
                .orElseThrow(() -> new RuntimeException("Ссылка не найдена"));

        if (original == null) throw new RuntimeException("Ссылка не найдена");

        return ResponseEntity.status(302).location(URI.create(original)).build();
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
                .orElseThrow(() -> new RuntimeException("Ссылка не найдена"));

        LinkDto linkDto = linkMapper.toDto(result);
        return new ResponseEntity<>(linkDto, HttpStatus.OK);
    }
}