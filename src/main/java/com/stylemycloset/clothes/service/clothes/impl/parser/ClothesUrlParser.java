package com.stylemycloset.clothes.service.clothes.impl.parser;

import com.stylemycloset.clothes.dto.ClothesExtractedMetaInfo;
import java.io.IOException;
import java.util.EnumMap;
import org.jsoup.nodes.Element;
import org.jsoup.parser.StreamParser;
import org.springframework.stereotype.Component;

@Component
public class ClothesUrlParser {

  public ClothesExtractedMetaInfo extract(
      StreamParser streamer,
      String metaTagSelector,
      String attributeKeyName,
      String attributeValueName
  ) throws IOException {
    EnumMap<OpenGraphProperty, String> parseResult = new EnumMap<>(OpenGraphProperty.class);

    Element element;
    while ((element = streamer.selectNext(metaTagSelector)) != null) {
      String property = getProperty(element, attributeKeyName);
      String content = getProperty(element, attributeValueName);
      if (property.isBlank() || content.isBlank()) {
        continue;
      }

      OpenGraphProperty.fromKey(property)
          .ifPresent(og ->
              parseResult.computeIfAbsent(og, k -> content)
          );

      element.remove();
    }

    return new ClothesExtractedMetaInfo(
        parseResult.get(OpenGraphProperty.META_OG_TITLE),
        parseResult.get(OpenGraphProperty.META_OG_IMAGE)
    );
  }

  private String getProperty(Element element, String attributeKey) {
    return element.attr(attributeKey).trim();
  }

}
