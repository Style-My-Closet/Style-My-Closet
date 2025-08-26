package com.stylemycloset.clothes.service.clothes.impl;

import static com.stylemycloset.clothes.service.extractor.impl.parser.JsoupSelectorConstant.META_ATTRIBUTE_CONTENT;
import static com.stylemycloset.clothes.service.extractor.impl.parser.JsoupSelectorConstant.META_ATTRIBUTE_PROPERTY;
import static com.stylemycloset.clothes.service.extractor.impl.parser.JsoupSelectorConstant.META_PROPERTY_SELECTOR;

import com.stylemycloset.clothes.dto.ClothesExtractedMetaInfo;
import com.stylemycloset.clothes.service.extractor.impl.parser.ClothesUrlParser;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.jsoup.parser.Parser;
import org.jsoup.parser.StreamParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClothesUrlParserTest {

  private ClothesUrlParser clothesUrlParser;

  @BeforeEach
  void setUp() {
    clothesUrlParser = new ClothesUrlParser();
  }

  @DisplayName("og:title, og:image 정상 추출")
  @Test
  void test() throws IOException {
    // given
    String expectedTitle = "테스트 제목";
    String expectedImageUrl = "https://cdn.example.com/img.jpg";
    String html = String.format("""
        <html><head>
          <meta property="og:title" content="%s">
          <meta property="og:image" content="%s">
        </head><body></body></html>
        """, expectedTitle, expectedImageUrl);

    StreamParser streamParser = new StreamParser(Parser.htmlParser())
        .parse(html, "https://base/");

    // when
    ClothesExtractedMetaInfo info = clothesUrlParser.extract(
        streamParser,
        META_PROPERTY_SELECTOR,
        META_ATTRIBUTE_PROPERTY,
        META_ATTRIBUTE_CONTENT
    );

    // then
    Assertions.assertThat(info.productName()).isEqualTo(expectedTitle);
    Assertions.assertThat(info.imageUrl()).isEqualTo(expectedImageUrl);
  }

  @Test
  @DisplayName("동일 property가 여러 번 등장해도 첫 번째만 유지 (computeIfAbsent)")
  void keepsFirstOccurrenceOnly() throws Exception {
    // given
    String firstTitle = "첫번째 제목";
    String secondTitle = "두번째 제목";
    String firstImageUrl = "https://first.example.com/a.jpg";
    String secondImageUrl = "https://second.example.com/b.jpg";
    String html = String.format("""
        <html><head>
          <meta property="og:image" content="%s">
          <meta property="og:image" content="%s">
          <meta property="og:title" content="%s">
          <meta property="og:title" content="%s">
        </head></html>
        """, firstImageUrl, secondImageUrl, firstTitle, secondTitle);

    StreamParser streamParser = new StreamParser(Parser.htmlParser())
        .parse(html, "https://base/");

    // when
    ClothesExtractedMetaInfo info = clothesUrlParser.extract(
        streamParser,
        META_PROPERTY_SELECTOR,
        META_ATTRIBUTE_PROPERTY,
        META_ATTRIBUTE_CONTENT
    );

    // then
    Assertions.assertThat(info.productName()).isEqualTo(firstTitle);
    Assertions.assertThat(info.imageUrl()).isEqualTo(firstImageUrl);
  }

  @Test
  @DisplayName("og 메타가 없을 때는 null 필드")
  void returnsNullsWhenNotFound() throws Exception {
    // given
    String html = """
        <html><head>
          <meta name="description" content="desc only">
        </head></html>
        """;
    StreamParser streamParser = new StreamParser(Parser.htmlParser())
        .parse(html, "https://base/");

    // when
    ClothesExtractedMetaInfo info = clothesUrlParser.extract(
        streamParser,
        META_PROPERTY_SELECTOR,
        META_ATTRIBUTE_PROPERTY,
        META_ATTRIBUTE_CONTENT
    );

    // then
    Assertions.assertThat(info.productName()).isNull();
    Assertions.assertThat(info.imageUrl()).isNull();
  }

  @Test
  @DisplayName("name 속성의 meta는 선택자(head > meta[property])에 의해 무시됨")
  void ignoresNameMetaWithCurrentSelector() throws Exception {
    // given
    String nameBasedTitle = "name-기반 제목(무시)";
    String nameBasedImage = "https://img/ignored.jpg";
    String propertyBasedTitle = "property-기반 제목(채택)";
    String propertyBasedImage = "https://img/used.jpg";
    String html = String.format("""
        <html><head>
          <meta name="og:title" content="%s">
          <meta name="og:image" content="%s">
          <meta property="og:title" content="%s">
          <meta property="og:image" content="%s"
          >
        </head></html>
        """, nameBasedTitle, nameBasedImage, propertyBasedTitle, propertyBasedImage);

    StreamParser streamParser = new StreamParser(Parser.htmlParser())
        .parse(html, "https://base/");

    // when
    ClothesExtractedMetaInfo info = clothesUrlParser.extract(
        streamParser,
        META_PROPERTY_SELECTOR,
        META_ATTRIBUTE_PROPERTY,
        META_ATTRIBUTE_CONTENT
    );

    // then
    Assertions.assertThat(info.productName()).isEqualTo(propertyBasedTitle);
    Assertions.assertThat(info.imageUrl()).isEqualTo(propertyBasedImage);
  }

}