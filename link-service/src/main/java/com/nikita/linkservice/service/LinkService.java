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
import java.util.Random;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;

    public LinkService(LinkRepository linkRepository, LinkMapper linkMapper) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
    }

    @Transactional
    public ResponseEntity<LinkResponse> generate(LinkRequest request) {
        LinkEntity entity = LinkEntity.builder()
                .original(request.getOriginal())
                .link(shortLinkGenerator())
                .count(0L) // TODO вынести в БД default = 0f
                .build();

        entity = linkRepository.save(entity);
        return ResponseEntity.ok(new LinkResponse("/l/" + entity.getLink()));
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