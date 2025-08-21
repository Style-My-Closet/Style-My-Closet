package com.stylemycloset.clothes.mapper;

import com.stylemycloset.binarycontent.mapper.BinaryContentMapper;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import com.stylemycloset.clothes.dto.clothes.AttributeDto;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.repository.clothes.cursor.ClothesField;
import com.stylemycloset.common.repository.cursor.CursorStrategy;
import com.stylemycloset.common.repository.cursor.NextCursorInfo;
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
    List<AttributeDto> attributes = clothes.getSelectedValues() // TODO: 8/17/25 N+1 해결 바람
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
    List<ClothesDto> content = clothes.getContent()
        .stream()
        .map(this::toResponse)
        .toList();
    Order order = getOrder(clothes);

    return ClothDtoCursorResponse.of(
        content,
        extractNextCursorInfo(clothes, order.getProperty()),
        clothes.hasNext(),
        null,
        order.getProperty(),
        order.getDirection().toString()
    );
  }

  private Order getOrder(Slice<Clothes> clothes) {
    return clothes.getPageable()
        .getSort()
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("DTO 변환시 정렬 순서(Order)가 존재하지 않습니다."));
  }

  // 에는 nextCursor로 빼고
  private NextCursorInfo extractNextCursorInfo(Slice<Clothes> clothes, String sortBy) {
    if (sortBy == null || sortBy.isBlank() ||
        !clothes.hasNext() || clothes.getContent().isEmpty()
    ) {
      return new NextCursorInfo(null, null);
    }

    Clothes lastMessage = clothes.getContent().get(clothes.getContent().size() - 1);

    CursorStrategy<?, Clothes> clothesCursorStrategy = ClothesField.resolveStrategy(sortBy);
    String cursor = clothesCursorStrategy.extract(lastMessage).toString();
    String idAfter = lastMessage.getId().toString();

    return new NextCursorInfo(cursor, idAfter);
  }

}
