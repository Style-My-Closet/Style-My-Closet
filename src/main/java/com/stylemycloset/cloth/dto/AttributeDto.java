package com.stylemycloset.cloth.dto;

import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingAttributeValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AttributeDto {
    private Long definitionId;  // 속성의 아이디
    private String definitionName; //속성의 이름
    private List<String> selectableValues; // 각 옵션의 리스트 값
    private String value; // 옵션의 리스트 값에서 선택된 값

    public static AttributeDto from(ClothingAttributeValue clothingAttributeValue) {
        ClothingAttribute attribute = clothingAttributeValue.getAttribute();
        Long defId = attribute.getId();
        String defName = attribute.getName();

        List<String> selectList = attribute.getOptions()
                .stream()
                .map(AttributeOption::getValue)
                .toList();

        String val = clothingAttributeValue.getOption().getValue();
        return new AttributeDto(defId, defName, selectList, val);
    }
}
