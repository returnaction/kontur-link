package com.nikita.linkservice.mapper;

import com.nikita.linkservice.model.dto.LinkDto;
import com.nikita.linkservice.model.entity.LinkEntity;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    public LinkDto toDto(LinkEntity entity) {
        return LinkDto.builder()
                .original(entity.getOriginal())
                .link(entity.getLink())
                .count(entity.getCount())
                .build();
    }
}
