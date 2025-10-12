package com.nikita.linkservice.repository;


import com.nikita.linkservice.model.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface LinkRepository extends JpaRepository<LinkEntity, UUID> {
    Optional<LinkEntity> findByLink(String link);
}
