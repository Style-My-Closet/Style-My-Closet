package com.stylemycloset.ootd.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;
import java.util.Map;

public class FeedNotFoundException extends StyleMyClosetException {

  public FeedNotFoundException(Long feedId) {
    super(ErrorCode.FEED_NOT_FOUND, Map.of("feedId", feedId));
  }
}
