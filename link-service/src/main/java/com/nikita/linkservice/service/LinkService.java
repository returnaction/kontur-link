package com.nikita.linkservice.service;

import com.nikita.linkservice.mapper.LinkMapper;
import com.nikita.linkservice.model.dto.*;
import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.LinkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final JdbcTemplate jdbcTemplate;


    public LinkService(LinkRepository linkRepository, LinkMapper linkMapper, JdbcTemplate jdbcTemplate) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ResponseEntity<LinkResponse> generate(LinkRequest request) {
        LinkEntity entity = LinkEntity.builder()
                .original(request.getOriginal())
                .link(shortLinkGenerator())
                .count(0L)
                .build();

        entity = linkRepository.save(entity);

        LinkResponse response = LinkResponse.builder()
                .link("/l/" + entity.getLink())
                .build();

        return ResponseEntity.ok(response);
    }

    private String shortLinkGenerator() {
        int leftLimit = 97;
        int rightLimit = 122;
        int targetStringLength = 10;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);

        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }

        String generatedString = buffer.toString();
        System.out.println(">>>>> Generated Link: " + generatedString);
        return generatedString;
    }

    @Transactional
    public ResponseEntity<Void> redirect(String shortLink) {
        String original = linkRepository.incrementAndGetOriginal(shortLink)
                .orElseThrow(() -> new RuntimeException("Ссылка не найдена"));

        if (original == null) {
            throw new RuntimeException("Ссылка не найдена");
        }

        return ResponseEntity
                .status(302)
                .location(URI.create(original))
                .build();
    }

    //TODO сделать маппер для Map<String, Object> to LinkDTO или c
    @Transactional(readOnly = true)
    public ResponseEntity<List<LinkDto>> getStats() {
        List<Map<String, Object>> rawLinks = linkRepository.findAllLinksWithStatsOrderByCountDesc();
        List<LinkDto> dtoLinks = rawLinks.stream()
                .map(raw -> LinkDto.builder()
                        .link(raw.get("link").toString())
                        .original(raw.get("original").toString())
                        .count((long) raw.get("count"))
                        .rank((long) raw.get("rank"))
                        .build())
                .toList();
        return ResponseEntity.ok(dtoLinks);
    }

    @Transactional(readOnly = true)
    public ResponseEntity<LinkDto> getStat(String shortLink) {
        Map<String, Object> linkWithStatsByShortLink = linkRepository.findLinkWithStatsByShortLink(shortLink);
        LinkDto build = LinkDto.builder()
                .link(linkWithStatsByShortLink.get("link").toString())
                .original(linkWithStatsByShortLink.get("original").toString())
                .count((long) linkWithStatsByShortLink.get("count"))
                .rank((long) linkWithStatsByShortLink.get("rank"))
                .build();

        return new ResponseEntity<>(build, HttpStatus.OK);
    }
}
