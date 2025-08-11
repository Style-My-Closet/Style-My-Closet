package com.stylemycloset.common.config.taskdecorator;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    SecurityContext securityContext = SecurityContextHolder.getContext();

    return () -> {
      try {
        SecurityContextHolder.setContext(securityContext);
        runnable.run();
      } finally {
        SecurityContextHolder.clearContext();
      }
    };
  }
}
