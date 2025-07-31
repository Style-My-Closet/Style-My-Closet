package com.stylemycloset.cloth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import com.stylemycloset.common.entity.BaseEntity;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clothes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cloth extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long clothId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "closet_id", nullable = false)
    private Closet closet;

    //   참조 값만 다루기 위해
    @Column(name = "binary_content")
    private long binaryContent;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ClothingCategory category;

    @OneToMany(mappedBy = "cloth", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ClothingAttributeValue> attributeValues = new ArrayList<>();
}