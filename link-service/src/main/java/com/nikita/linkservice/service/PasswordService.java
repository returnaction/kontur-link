package com.nikita.linkservice.service;

import com.nikita.linkservice.repository.LinkRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordService {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int TOKEN_LEN = 10;
    private static final SecureRandom RNG = new SecureRandom();
    private final LinkRepository linkRepository;

    public PasswordService(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    public String generateUniqueToken() {
        String token;
        do {
            token = generateToken();

        } while (linkRepository.existsByLink(token));
        return token;
    }

    private String generateToken() {
        StringBuilder sb = new StringBuilder(TOKEN_LEN);
        for (int i = 0; i < TOKEN_LEN; i++) {
            int index = RNG.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }
}