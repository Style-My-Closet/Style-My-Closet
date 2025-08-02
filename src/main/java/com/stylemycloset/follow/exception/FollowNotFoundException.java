package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public class FollowNotFoundException extends FollowException {

  public FollowNotFoundException(Map<String, Object> details) {
    super(ErrorCode.ERROR_FOLLOW_NOT_FOUND, details);
  }

}
