package com.nikita.linkservice.repository.projection;


public interface LinkStatsView {
    String getLink();

    String getOriginal();

    Long getRank();

    Long getCount();
}