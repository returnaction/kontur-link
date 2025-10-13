package com.nikita.linkservice.repository;


import com.nikita.linkservice.model.entity.LinkEntity;
import com.nikita.linkservice.repository.projection.LinkStatsView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

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
            SELECT l.link            AS link,
                   l.original        AS original,
                   l.count           AS count,
                   DENSE_RANK() OVER (ORDER BY l.count DESC) AS rank
            FROM links l
            ORDER BY l.count DESC
            """,
            countQuery = "SELECT COUNT(*) FROM links",
            nativeQuery = true)
    Page<LinkStatsView> findAllStats(Pageable pageable);

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
    Optional<LinkStatsView> findStatsByLink(@Param("link") String link);
}
