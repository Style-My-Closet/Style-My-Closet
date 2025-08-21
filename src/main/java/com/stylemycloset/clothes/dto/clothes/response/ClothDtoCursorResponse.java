package com.stylemycloset.clothes.dto.clothes.response;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.common.repository.cursor.NextCursorInfo;
import java.util.List;

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
      String sortBy,
      String sortDirection
  ) {
    return new ClothDtoCursorResponse(
        data,
        nextCursorInfo.nextCursor(),
        nextCursorInfo.nextIdAfter(),
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }

}
