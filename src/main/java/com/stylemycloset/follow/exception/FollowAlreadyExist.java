package com.stylemycloset.follow.exception;

import com.stylemycloset.common.exception.ErrorCode;
import java.util.Map;

public class FollowAlreadyExist extends FollowException {

  public FollowAlreadyExist(Map<String, Object> details) {
    super(ErrorCode.ERROR_FOLLOW_ALREADY_EXIST, details);
  }

}
