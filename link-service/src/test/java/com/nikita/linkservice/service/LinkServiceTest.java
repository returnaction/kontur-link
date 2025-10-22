package com.nikita.linkservice.service;

import com.nikita.linkservice.exception.BadRequestException;
import com.nikita.linkservice.mapper.LinkMapper;
import com.nikita.linkservice.model.dto.LinkRequest;
import com.nikita.linkservice.model.dto.LinkResponse;
import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest  {

    @Mock
    private LinkRepository linkRepository;
    @Mock
    private LinkMapper linkMapper;
    @Mock
    private RedisService redisService;
    @Mock
    private PasswordService passwordService;
    @InjectMocks
    private LinkService linkService;

    @Test
    void generate_shouldTrowBadRequest_whenOriginalIsNull() {
        LinkRequest linkRequest = new LinkRequest();
        linkRequest.setOriginal(null);

        assertThrows(BadRequestException.class, () -> linkService.generate(linkRequest));
    }

    @Test
    void generate_shouldThrowBadRequest_whenOriginalIsEmpty(){
        LinkRequest linkRequest = new LinkRequest();
        linkRequest.setOriginal("   ");

        assertThrows(BadRequestException.class, () -> linkService.generate(linkRequest));
    }

    @Test
    void generate_shouldCreateNewLink_whenOriginalIsNotExists() {
        LinkRequest linkRequest = new LinkRequest();
        linkRequest.setOriginal("mak.by");

        when(passwordService.generateUniqueToken()).thenReturn("qwertyuiop");
        when(linkRepository.save(any(LinkEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<LinkResponse> response = linkService.generate(linkRequest);

        assertNotNull(response);
        assertEquals("/l/qwertyuiop", response.getBody().getLink());
        verify(linkRepository).save(any(LinkEntity.class));
    }

    @Test
    void update() {
    }

    @Test
    void redirect() {
    }

    @Test
    void delete() {
    }

    @Test
    void getStats() {
    }

    @Test
    void getStat() {
    }
}