package com.stylemycloset.common.web;

import org.hibernate.query.SortDirection;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfigFormatter implements WebMvcConfigurer {

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new Converter<String, Direction>() {
      @Override
      public Direction convert(String rawDirection) {
        SortDirection sortDirection = getSortDirection(rawDirection);
        if (sortDirection == null || sortDirection.name().startsWith(Direction.DESC.name())) {
          return Direction.DESC;
        }
        return Direction.ASC;
      }
    });
  }

  private SortDirection getSortDirection(String rawDirection) {
    try {
      return SortDirection.interpret(rawDirection);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

}