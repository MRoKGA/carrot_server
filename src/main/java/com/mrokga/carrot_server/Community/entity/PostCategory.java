package com.mrokga.carrot_server.Community.entity;

import jakarta.persistence.*;
import lombok.*;

@Table(name = "communityCategory")
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 20)
    private String name;
}
