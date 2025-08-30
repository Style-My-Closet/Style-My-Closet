package com.stylemycloset.clothes.service.extractor.impl.parser;

import static com.stylemycloset.clothes.service.extractor.impl.parser.ExtractTargetMetaInfo.META_IMAGE_URL;
import static com.stylemycloset.clothes.service.extractor.impl.parser.ExtractTargetMetaInfo.META_TITLE;

import com.stylemycloset.clothes.dto.ClothesExtractedMetaInfo;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import org.jsoup.nodes.Element;
import org.jsoup.parser.StreamParser;
import org.springframework.stereotype.Component;

@Component
public class ClothesUrlParser {

  private static final EnumSet<ExtractTargetMetaInfo> REQUIRED = EnumSet.of(
      META_TITLE, META_IMAGE_URL
  );

  public ClothesExtractedMetaInfo extract(
      StreamParser streamer,
      String metaTagSelector,
      String attributeKeyName,
      String attributeValueName
  ) throws IOException {
    EnumMap<ExtractTargetMetaInfo, String> parseResult = new EnumMap<>(ExtractTargetMetaInfo.class);

    Element element;
    while ((element = streamer.selectNext(metaTagSelector)) != null) {
      String property = getProperty(element, attributeKeyName);
      String content = getProperty(element, attributeValueName);
      if (property.isBlank() || content.isBlank()) {
        continue;
      }

      ExtractTargetMetaInfo.fromKey(property)
          .ifPresent(og ->
              parseResult.computeIfAbsent(og, k -> content)
          );

      if (parseResult.keySet().containsAll(REQUIRED)) {
        return buildMetaInfo(parseResult);
      }
    }

    return buildMetaInfo(parseResult);
  }

  private ClothesExtractedMetaInfo buildMetaInfo(
      EnumMap<ExtractTargetMetaInfo, String> parseResult) {
    return new ClothesExtractedMetaInfo(
        parseResult.get(META_TITLE),
        parseResult.get(META_IMAGE_URL)
    );
  }

  private String getProperty(Element element, String attributeKey) {
    return element.attr(attributeKey).trim();
  }

}
