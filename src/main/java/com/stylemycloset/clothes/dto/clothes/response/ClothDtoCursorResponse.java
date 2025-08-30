package com.stylemycloset.clothes.dto.clothes.response;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.common.repository.NextCursorInfo;
import java.util.List;
import org.springframework.data.domain.Sort.Order;

public record ClothDtoCursorResponse(
    List<ClothesDto> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Long totalCount,
    String sortBy,
    String sortDirection
) {

  public static ClothDtoCursorResponse of(
      List<ClothesDto> data,
      NextCursorInfo nextCursorInfo,
      Boolean hasNext,
      Long totalCount,
      Order order
  ) {
    return new ClothDtoCursorResponse(
        data,
        nextCursorInfo.nextCursor(),
        nextCursorInfo.nextIdAfter(),
        hasNext,
        totalCount,
        getSortBy(order),
        getSortDirection(order)
    );
  }

  private static String getSortBy(Order order) {
    if (order == null) {
      return null;
    }
    return order.getProperty();
  }

  private static String getSortDirection(Order order) {
    if (order == null) {
      return null;
    }
    return order.getDirection()
        .toString();
  }

}
