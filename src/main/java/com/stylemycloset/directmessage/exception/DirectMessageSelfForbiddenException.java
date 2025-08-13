package com.stylemycloset.directmessage.exception;

import com.stylemycloset.common.exception.ErrorCode;

public final class DirectMessageSelfForbiddenException extends DirectMessageException {

  public DirectMessageSelfForbiddenException() {
    super(ErrorCode.ERROR_DIRECT_SELF_FORBIDDEN);
  }

}
