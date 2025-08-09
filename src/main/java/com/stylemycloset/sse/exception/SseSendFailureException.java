package com.stylemycloset.sse.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public class SseSendFailureException extends StyleMyClosetException {

  public SseSendFailureException(Long userId, String eventId) {
    super(ErrorCode.SSE_SEND_FAILURE);
    addAllDetails(Map.of("userId", userId, "eventId", eventId));
  }
}
