package com.stylemycloset.cloth.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.stylemycloset.common.entity.BaseEntity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes_attributes_categories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClothingCategory  extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long categoryId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name="deleted_at",columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant deletedAt;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id",nullable = false)
    private List<Cloth> clothes = new ArrayList<>();

}