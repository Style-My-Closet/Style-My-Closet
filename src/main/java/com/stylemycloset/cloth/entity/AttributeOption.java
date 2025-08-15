package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clothes_attributes_category_options")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttributeOption extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_option_seq_gen")
    @SequenceGenerator(name = "attribute_option_seq_gen", sequenceName = "clothes_attributes_category_options_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private ClothingAttribute attribute;

    @Column(nullable = false, length = 50)
    private String value;

    // attributeValues 컬렉션 제거됨 (단방향으로 개선)
    // 필요시 Repository에서 조회: findByOption(AttributeOption option)

    public static AttributeOption createOption(ClothingAttribute attribute, String value) {
        if (attribute == null) return null;
        if (value == null || value.isBlank()) return null;

        AttributeOption option = AttributeOption.builder()
                .attribute(attribute)
                .value(value)
                .build();

        // 양방향 관계 동기화 (속성과 옵션은 항상 함께 사용)
        if (attribute.getOptions() != null) {
            attribute.getOptions().add(option);
        }
        return option;
    }

    @Override
    public void softDelete() {
        super.softDelete();
        // 양방향 관계 정리 (AttributeValue는 서비스에서 처리)
        if (attribute != null && attribute.getOptions() != null) {
            attribute.getOptions().remove(this);
        }
    }

}
