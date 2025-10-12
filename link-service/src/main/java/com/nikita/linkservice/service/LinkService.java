package com.nikita.linkservice.service;

import com.nikita.linkservice.model.dto.LinkRequest;
import com.nikita.linkservice.model.dto.LinkResponse;
import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.LinkRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class LinkService {

    private final LinkRepository linkRepository;

    public LinkService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Transactional
    public ResponseEntity<LinkResponse> generate(LinkRequest request) {
        String original = request.getOriginal();
        String shortLink = shortLinkGenerator(original);

        LinkEntity entity = LinkEntity.builder()
                .linkId(UUID.randomUUID())
                .original(original.toString())
                .link(shortLink)
                .build();

        entity = linkRepository.save(entity);

        LinkResponse response = LinkResponse.builder()
                .link(entity.getLink())
                .build();

        return ResponseEntity.ok(response);
    }

    private String shortLinkGenerator(String link) {
        StringBuilder result = new StringBuilder();
        result.append("/l/");

        Matcher matcher = Pattern.compile("https?://([^/]+)").matcher(link);
        if (matcher.find()) {
            result.append(matcher.group(1));
        }
        return result.toString();
    }

    @Transactional
    public ResponseEntity<Void> redirect(String shortLink) {
        LinkEntity entity = linkRepository.findByLink("/l/" + shortLink).orElseThrow(() -> new RuntimeException("Ссылка не найдена"));

        entity.setCount(entity.getCount() + 1);
        linkRepository.save(entity);

        return ResponseEntity.status(302)
                .location(URI.create(entity.getOriginal()))
                .build();
    }
}
