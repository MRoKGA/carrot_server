package com.mrokga.carrot_server.Product.entity;

import com.mrokga.carrot_server.Region.entity.Region;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_exposure_region")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductExposureRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;

}