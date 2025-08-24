package com.stylemycloset.clothes.mapper;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.clothes.dto.clothes.AttributeDto;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.common.repository.CustomSliceImpl;
import com.stylemycloset.common.repository.NextCursorInfo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesMapper {

  private final BinaryContentMapper binaryContentMapper;

  public ClothesDto toResponse(Clothes clothes) {
    List<AttributeDto> attributes = clothes.getSelectedValues()
        .stream()
        .map(AttributeDto::from)
        .toList();

    return new ClothesDto(
        clothes.getId(),
        clothes.getOwnerId(),
        clothes.getName(),
        binaryContentMapper.extractUrl(clothes.getImage()),
        clothes.getClothesType(),
        attributes
    );
  }

  public ClothDtoCursorResponse toPageResponse(Slice<Clothes> clothes) {
    List<ClothesDto> content = getClotheDtos(clothes);
    Order order = CustomSliceImpl.getOrder(clothes);

    return ClothDtoCursorResponse.of(
        content,
        NextCursorInfo.clothesCursor(clothes, order.getProperty()),
        clothes.hasNext(),
        null,
        order
    );
  }

  private List<ClothesDto> getClotheDtos(Slice<Clothes> clothes) {
    return clothes.getContent()
        .stream()
        .map(this::toResponse)
        .toList();
  }

}
