package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;

public final class FollowAlreadyExistException extends FollowException {

  public FollowAlreadyExistException() {
    super(ErrorCode.ERROR_FOLLOW_ALREADY_EXIST);
  }

}
