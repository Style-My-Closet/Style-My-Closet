package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public final class ActiveFollowNotFoundException extends FollowException {

  public ActiveFollowNotFoundException() {
    super(ErrorCode.ERROR_ACTIVE_FOLLOW_NOT_FOUND);
  }

}
