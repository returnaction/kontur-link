package com.nikita.linkservice.model.dto;

import lombok.*;

@Data
@Builder
public class LinkDto {
    private String original;
    private String link;
    private Long count;
    private Long rank;
}
