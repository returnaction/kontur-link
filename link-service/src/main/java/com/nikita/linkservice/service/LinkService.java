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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Optional;

@Service
public class LinkService {

    private final LinkRepository linkRepository;
    private final LinkMapper linkMapper;
    private final RedisService redisService;
    private final PasswordService passwordService;

    public LinkService(LinkRepository linkRepository, LinkMapper linkMapper, RedisService redisService, PasswordService passwordService) {
        this.linkRepository = linkRepository;
        this.linkMapper = linkMapper;
        this.redisService = redisService;
        this.passwordService = passwordService;
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

        String token = passwordService.generateUniqueToken();

        LinkEntity entity = LinkEntity.builder()
                .original(request.getOriginal())
                .link(token)
                .count(0L)
                .build();
        entity = linkRepository.save(entity);

        redisService.addCashLink(token, entity.getLink());

        return ResponseEntity.ok(new LinkResponse("/l/" + entity.getLink()));
    }


    @Transactional(timeout = 5)
    public ResponseEntity<LinkResponse> update(String shortLink, LinkRequest request) {
        if (shortLink == null || shortLink.isBlank())
            throw new BadRequestException("Не предоставил короткую ссылку");
        if (request.getOriginal() == null)
            throw new BadRequestException("Поле 'original' не может быть пустым");

        LinkEntity entity = linkRepository.findByLinkForUpdate(shortLink)
                .orElseThrow(() -> new ShortLinkNotFoundException(shortLink));

        entity.setOriginal(request.getOriginal());
        entity = linkRepository.save(entity);

        redisService.addCashLink(entity.getLink(), entity.getOriginal());

        return ResponseEntity.ok(new LinkResponse("/l/" + entity.getLink()));
    }


    @Transactional
    public ResponseEntity<Void> redirect(String shortLink) {
        String original = redisService.getCacheLink(shortLink);
        if (original == null) {
            original = linkRepository.findOriginalByLink(shortLink)
                    .orElseThrow(() -> new ShortLinkNotFoundException(shortLink));

            redisService.addCashLink(shortLink, original);
            System.out.println("Взято из бд");
        }

        linkRepository.incrementLinkCount(shortLink);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(original)).build();
    }


    @Transactional(timeout = 5)
    public ResponseEntity<Void> delete(String shortLink) {
        if (shortLink == null || shortLink.isBlank())
            throw new BadRequestException("Не предоставил короткую ссылку");

        LinkEntity entity = linkRepository.findByLinkForUpdate(shortLink)
                .orElseThrow(() -> new ShortLinkNotFoundException(shortLink));

        linkRepository.delete(entity);
        redisService.removeCacheLink(shortLink);
        return ResponseEntity.noContent().build();
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