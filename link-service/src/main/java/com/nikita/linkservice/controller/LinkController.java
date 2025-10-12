package com.nikita.linkservice.controller;

import com.nikita.linkservice.model.dto.*;
import com.nikita.linkservice.service.LinkService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/generate")
    public ResponseEntity<LinkResponse> generateLink(@RequestBody LinkRequest request){
        return linkService.generate(request);
    }

    @GetMapping("/l/{short_link}")
    public ResponseEntity<Void> redirect(@PathVariable String short_link){
        return linkService.redirect(short_link);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<LinkDto>> stats(){
        return linkService.getStats();
    }

    @GetMapping("/stats/l/{short_link}")
    public ResponseEntity<LinkDto> stats(@PathVariable String short_link){
        return linkService.getStat(short_link);
    }
}
