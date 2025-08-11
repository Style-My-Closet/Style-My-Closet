package com.stylemycloset.cloth.entity;

import com.stylemycloset.common.entity.SoftDeletableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY)
    @Where(clause = "deleted_at IS NULL")
    @Builder.Default
    private List<ClothingAttributeValue> attributeValues = new ArrayList<>();


    public static void createOption(ClothingAttribute attribute, String value) {
        if (attribute == null) return;
        if (value == null || value.isBlank()) return;

        AttributeOption option = AttributeOption.builder()
                .attribute(attribute)
                .value(value)
                .build();

        attribute.getOptions().add(option);

    }


    @Override
    public void softDelete() {
        super.softDelete();
        // 관계된 엔티티들의 메모리상 컬렉션에서 제거
        if (attribute != null && attribute.getOptions() != null) {
            attribute.getOptions().remove(this);
        }
        if (attributeValues != null) {
            attributeValues.forEach(ClothingAttributeValue::softDelete);
            attributeValues.clear();
        }
    }

}
