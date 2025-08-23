package com.stylemycloset.clothes.service.extractor.impl.parser;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum OpenGraphProperty {

  META_OG_TITLE("og:title"),
  META_OG_IMAGE("og:image");

  private final String key;

  OpenGraphProperty(String key) {
    this.key = key;
  }

  public static Optional<OpenGraphProperty> fromKey(String key) {
    return Arrays.stream(values())
        .filter(property -> property.key.equals(key))
        .findFirst();
  }

}
