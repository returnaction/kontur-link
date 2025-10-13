package com.nikita.linkservice.repository;


import com.nikita.linkservice.model.entity.LinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface LinkRepository extends JpaRepository<LinkEntity, UUID> {

    @Query(value = """
            WITH updated AS (
              UPDATE links
                 SET count = count + 1
               WHERE link = :link
             RETURNING original
            )
            SELECT original FROM updated
            """, nativeQuery = true)
    Optional<String> incrementAndGetOriginal(@Param("link") String link);

    @Query(value = """
                    select
                        l.link,
                        l.original,
                        l.count,
                        rank() over (order by l.count desc) as rank
                    from links l
                    order by l.count desc
            """,
            nativeQuery = true)
    List<Map<String, Object>> findAllLinksWithStatsOrderByCountDesc();

    @Query(value = """
            SELECT l.link,
                   l.original,
                   l.count,
                   1 + (
                       SELECT COUNT(*) FROM links l2
                       WHERE l2.count > l.count
                   ) AS rank
            FROM links l
            WHERE l.link = :link
            """, nativeQuery = true)
    Map<String, Object> findLinkWithStatsByShortLink(@Param("link") String link);


}
