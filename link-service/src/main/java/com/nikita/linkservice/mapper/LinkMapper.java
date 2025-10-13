package com.nikita.linkservice.mapper;

import com.nikita.linkservice.model.dto.LinkDto;
import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.projection.LinkStatsView;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper {

    public LinkDto toDto(LinkEntity entity) {
        if(entity == null) return null;
        return LinkDto.builder()
                .original(entity.getOriginal())
                .link(entity.getLink())
                .count(entity.getCount())
                .build();
    }

    public LinkDto toDto(LinkStatsView view){
        if(view == null) return null;
        return LinkDto.builder()
                .original(view.getOriginal())
                .link(view.getLink())
                .count(view.getCount())
                .rank(view.getRank())
                .build();
    }

    public Page<LinkDto> toDto(Page<LinkStatsView> page){
        return page.map(this::toDto);
    }
}