package com.stylemycloset.common.config.async;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MDCContextTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();

    return () -> {
      try {
        if (mdcContext != null) {
          MDC.setContextMap(mdcContext);
        }

        runnable.run();
      } finally {
        MDC.clear();
      }
    };
  }

}
