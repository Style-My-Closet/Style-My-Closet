package com.stylemycloset.clothes.service.extractor.impl;

import static com.stylemycloset.clothes.service.extractor.impl.parser.JsoupSelectorConstant.META_ATTRIBUTE_CONTENT;
import static com.stylemycloset.clothes.service.extractor.impl.parser.JsoupSelectorConstant.META_ATTRIBUTE_PROPERTY;
import static com.stylemycloset.clothes.service.extractor.impl.parser.JsoupSelectorConstant.META_PROPERTY_SELECTOR;

import com.stylemycloset.clothes.dto.ClothesExtractedMetaInfo;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.exception.InvalidClothesMetaInfoException;
import com.stylemycloset.clothes.service.extractor.ClothesInfoExtractService;
import com.stylemycloset.clothes.service.extractor.impl.parser.ClothesUrlParser;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.parser.StreamParser;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothesInfoExtractServiceImpl implements ClothesInfoExtractService {

  private final ClothesUrlParser clothesURLParser;

  @Retryable(
      retryFor = UncheckedIOException.class,
      maxAttempts = 3,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Override
  public ClothesDto extractInfo(String url) {
    try (StreamParser streamer = Jsoup.connect(url)
        .timeout(5_000)
        .execute()
        .streamParser()
    ) {
      ClothesExtractedMetaInfo metaInfo = clothesURLParser.extract(
          streamer,
          META_PROPERTY_SELECTOR,
          META_ATTRIBUTE_PROPERTY,
          META_ATTRIBUTE_CONTENT
      );
      validateParsedInfo(url, metaInfo);

      return ClothesDto.of(metaInfo);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void validateParsedInfo(String url, ClothesExtractedMetaInfo metaInfo) {
    if (metaInfo == null || metaInfo.productName() == null || metaInfo.imageUrl() == null) {
      throw new InvalidClothesMetaInfoException()
          .addDetails("request_url", url);
    }
  }

}
