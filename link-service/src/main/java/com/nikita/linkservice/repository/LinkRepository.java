package com.nikita.linkservice.repository;


import com.nikita.linkservice.model.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface LinkRepository extends JpaRepository<LinkEntity, UUID> {
    Optional<LinkEntity> findByLink(String link);

    @Query(value = """
            select * from links
            order by count desc""", nativeQuery = true)
    List<LinkEntity> findAllOrderByCountDesc();

    @Query(value = """
            select l.link, l.original, l.count,
                    rank() over (order by l.count desc) as rank
                            from links l 
                                    where l.link = :link""", nativeQuery = true)
    Map<String, Object> findLinkWithStatsByShortLink(@Param("link") String link);


}
