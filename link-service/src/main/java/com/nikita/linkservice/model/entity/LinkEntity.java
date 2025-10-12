package com.nikita.linkservice.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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

    @Size(max = 8192)
    private String original;

    @Column(updatable = false)
    private String link;

    private Long count;

}
