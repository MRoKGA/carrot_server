package com.mrokga.carrot_server.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 20)
    private String code;

    // POINT 타입은 JTS Geometry 또는 Hibernate Spatial로 매핑 가능
    @Column(columnDefinition = "POINT SRID 4326")
    private String centroid;
}
