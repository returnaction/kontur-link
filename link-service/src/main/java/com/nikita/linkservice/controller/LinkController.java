package com.nikita.linkservice.controller;

import com.nikita.linkservice.model.dto.*;
import com.nikita.linkservice.service.LinkService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/generate")
    public ResponseEntity<LinkResponse> generateLink(@RequestBody LinkRequest request) {
        return linkService.generate(request);
    }

    @GetMapping("/l/{short_link}")
    public ResponseEntity<Void> redirect(@PathVariable String short_link) {
        return linkService.redirect(short_link);
    }

    @GetMapping("/stats")
    public ResponseEntity<Page<LinkDto>> statsAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(name = "count", defaultValue = "20") int size) {
        return linkService.getStats(page, size);
    }

    @GetMapping("/stats/l/{short_link}")
    public ResponseEntity<LinkDto> stats(@PathVariable String short_link) {
        return linkService.getStat(short_link);
    }
}
