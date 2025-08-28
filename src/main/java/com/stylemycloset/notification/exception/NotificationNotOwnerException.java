package com.stylemycloset.notification.exception;

import com.stylemycloset.common.exception.ErrorCode;
import com.stylemycloset.common.exception.StyleMyClosetException;

public class NotificationNotOwnerException extends StyleMyClosetException {

  public NotificationNotOwnerException() {
    super(ErrorCode.NOTIFICATION_NOT_OWNER);
  }
}
