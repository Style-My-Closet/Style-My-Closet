package com.stylemycloset.clothes.service.extractor.impl;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.service.extractor.ClothesInfoExtractionService;
import java.io.IOException;
import java.io.UncheckedIOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.StreamParser;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class ClothesInfoExtractionServiceImpl implements ClothesInfoExtractionService {

  @Override
  public ClothesDto extractInfo(String url) {
    String name = null;
    String imageUrl = null;

    try (StreamParser streamer = Jsoup.connect(url)
        .execute()
        .streamParser()) {

      Element element;
      while ((element = streamer.selectNext("head > meta")) != null) {
        String property = element.attr("property");
        String content = element.attr("content");

        if (property.equals("og:title")) {
          name = content;
        }
        if (property.equals("og:image")) {
          imageUrl = content;
        }
        element.remove();
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    return ClothesDto.of(name, imageUrl);
  }

}


