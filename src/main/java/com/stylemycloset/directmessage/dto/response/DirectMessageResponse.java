package com.stylemycloset.directmessage.dto.response;

import com.stylemycloset.directmessage.dto.DirectMessageResult;
import java.util.List;

public record DirectMessageResponse<T>(
    List<T> data,
    String nextCursor,
    String nextIdAfter,
    Boolean hasNext,
    Integer totalCount,
    String sortBy,
    String sortDirection
) {

  public static DirectMessageResponse<DirectMessageResult> of(
      List<DirectMessageResult> messageResults,
      NextCursorInfo nextCursorInfo,
      Boolean hasNext,
      Integer totalCount,
      String sortBy,
      String sortDirection
  ) {
    return new DirectMessageResponse<>(
        messageResults,
        nextCursorInfo.nextCursor,
        nextCursorInfo.nextIdAfter,
        hasNext,
        totalCount,
        sortBy,
        sortDirection
    );
  }

  public record NextCursorInfo(String nextCursor, String nextIdAfter) {

  }

}
