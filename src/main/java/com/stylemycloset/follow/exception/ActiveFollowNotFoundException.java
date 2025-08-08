package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public class ActiveFollowNotFoundException extends FollowException {

  public ActiveFollowNotFoundException(Map<String, Object> details) {
    super(ErrorCode.ERROR_ACTIVE_FOLLOW_NOT_FOUND, details);
  }

}
