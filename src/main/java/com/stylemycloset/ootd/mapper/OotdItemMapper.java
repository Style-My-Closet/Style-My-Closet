package com.stylemycloset.ootd.mapper;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.clothes.dto.clothes.AttributeDto;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.ootd.dto.OotdItemDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OotdItemMapper {

  private final BinaryContentMapper binaryContentMapper;

  public OotdItemDto toDto(Clothes cloth) {
    if (cloth == null) {
      return null;
    }
    List<AttributeDto> attributes = getAttributes(cloth);

    return new OotdItemDto(
        cloth.getId(),
        cloth.getName(),
        binaryContentMapper.extractUrl(cloth.getImage()),
        cloth.getClothesType().name(),
        attributes
    );
  }

  public List<OotdItemDto> toDtoList(List<Clothes> clothes) {
    if (clothes == null) {
      return List.of();
    }

    return clothes.stream()
        .map(this::toDto)
        .collect(Collectors.toList());
  }

  private List<AttributeDto> getAttributes(Clothes cloth) {
    return cloth.getSelectedValues()
        .stream()
        .map(AttributeDto::from)
        .toList();
  }

}
