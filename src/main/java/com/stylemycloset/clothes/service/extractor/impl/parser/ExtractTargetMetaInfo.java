package com.stylemycloset.clothes.service.extractor.impl.parser;

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum ExtractTargetMetaInfo {

  META_TITLE("og:title"),
  META_IMAGE_URL("og:image");

  private final String key;

  ExtractTargetMetaInfo(String key) {
    this.key = key;
  }

  public static Optional<ExtractTargetMetaInfo> fromKey(String key) {
    return Arrays.stream(values())
        .filter(property -> property.key.equals(key))
        .findFirst();
  }

}
