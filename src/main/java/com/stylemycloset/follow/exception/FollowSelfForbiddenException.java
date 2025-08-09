package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public final class FollowSelfForbiddenException extends FollowException {

  public FollowSelfForbiddenException() {
    super(ErrorCode.ERROR_FOLLOW_SELF_FORBIDDEN);
  }

}
