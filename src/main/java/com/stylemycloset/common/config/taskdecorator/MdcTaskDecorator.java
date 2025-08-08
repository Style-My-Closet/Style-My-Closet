package com.stylemycloset.common.config.taskdecorator;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MdcTaskDecorator implements TaskDecorator {
  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> mdc = MDC.getCopyOfContextMap();

    return () -> {
      try {
        if (mdc != null) MDC.setContextMap(mdc);
        runnable.run();
      } finally {
        MDC.clear();
      }
    };
  }
}
