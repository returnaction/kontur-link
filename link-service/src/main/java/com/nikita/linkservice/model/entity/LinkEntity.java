package com.nikita.linkservice.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "links")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LinkEntity {

    @Id
    @Column(name = "link_id")
    private UUID linkId;

    @Column(unique = true, nullable = false)
    private String original;

    @Column(unique = true, nullable = false)
    private String link;

    private Long count;

    @PrePersist
    public void prePersist(){
        if(count == null) count = 0L;
    }

}
