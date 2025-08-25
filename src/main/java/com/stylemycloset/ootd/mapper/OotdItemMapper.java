package com.stylemycloset.ootd.mapper;

import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.cloth.entity.AttributeOption;
import com.stylemycloset.cloth.entity.Cloth;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.entity.ClothingCategoryType;
import com.stylemycloset.ootd.dto.ClothesAttributeWithDefDto;
import com.stylemycloset.ootd.dto.OotdItemDto;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OotdItemMapper {

    private final BinaryContentStorage binaryContentStorage;

    public OotdItemMapper(BinaryContentStorage binaryContentStorage) {
        this.binaryContentStorage = binaryContentStorage;
    }

    public OotdItemDto toDto(Cloth cloth) {
        if (cloth == null) {
            return null;
        }

        List<ClothesAttributeWithDefDto> attributes = mapAttributes(cloth);
        
        return new OotdItemDto(
                cloth.getId(),
                cloth.getName(),
                getImageUrl(cloth),
                mapClothesType(cloth),
                attributes);
    }

    public List<OotdItemDto> toDtoList(List<Cloth> clothes) {
        if (clothes == null) {
            return List.of();
        }

        return clothes.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private List<ClothesAttributeWithDefDto> mapAttributes(Cloth cloth) {
        return cloth.getAttributeValues().stream()
                .map(attributeValue -> {
                    ClothingAttribute definition = attributeValue.getAttribute();

                    List<String> selectableValues = definition.getOptions().stream()
                            .map(AttributeOption::getValue)
                            .collect(Collectors.toList());

                    String chosenValue = attributeValue.getOption().getValue();

                    return new ClothesAttributeWithDefDto(
                            definition.getId(),
                            definition.getName(),
                            selectableValues,
                            chosenValue);
                })
                .collect(Collectors.toList());
    }

    private ClothingCategoryType mapClothesType(Cloth cloth) {
        return cloth.getCategory().getName();
    }

    private String getImageUrl(Cloth cloth) {
        if (cloth.getBinaryContent() != null) {
            URL url = binaryContentStorage.getUrl(cloth.getBinaryContent().getId());
            return url != null ? url.toString() : null;
        }
        return null;
    }
}
