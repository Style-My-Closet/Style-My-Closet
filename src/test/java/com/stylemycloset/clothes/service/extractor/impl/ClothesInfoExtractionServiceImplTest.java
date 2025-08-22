package com.stylemycloset.clothes.service.extractor.impl;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.service.extractor.ClothesInfoExtractionService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ClothesInfoExtractionServiceImplTest {

  private static final String URL = "https://www.musinsa.com/products/4655539";

  @DisplayName("상품 URL 입력시, 기본적으로 이름과 이미지 URL 을 추출합니다.")
  @Test
  void test() {
    // given
    ClothesInfoExtractionService clothesInfoExtractionService = new ClothesInfoExtractionServiceImpl();

    // when
    ClothesDto clothesDto = clothesInfoExtractionService.extractInfo(URL);

    // then
    Assertions.assertThat(clothesDto)
        .extracting(ClothesDto::name, ClothesDto::imageUrl)
        .containsExactly(
            "러닝라이프(RIFE) [3PACK] 스포츠 헤어밴드 (일체형) - 사이즈 & 후기 | 무신사",
            "https://image.msscdn.net/images/goods_img/20241209/4655539/4655539_17336706758907_500.jpg"
        );
  }

}