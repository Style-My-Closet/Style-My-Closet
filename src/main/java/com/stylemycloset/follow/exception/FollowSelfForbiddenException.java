package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public class FollowSelfForbiddenException extends FollowException {

  public FollowSelfForbiddenException(Map<String, Object> details) {
    super(ErrorCode.ERROR_FOLLOW_SELF_FORBIDDEN, details);
  }

}
