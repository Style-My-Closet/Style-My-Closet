package com.stylemycloset.clothes.service.extract;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;

public interface ClothesProductExtractionService {

  /**
   *
   * 흐름
   * 목표 : 이미지와 사진만 뽑아 놓는다. 그리고 clothesDto로 반환합니다.
   *
   * 지금 흐름
   * 1. 최소 정보 추출 -> 어떤 정보를 추출할 수 있을까, 도메인에 따라 프론트엔드가 달라질 것 같은게 문제인데
   * 2. 여기서 대표 이미지하고, 상품명 받아오기
   * 3. 웹사이트에서 추출한 정보를 LLM을 사용해서 추정되는 속성 값들 받아오기
   * 4. 속성값 반환
   * 5. 파싱은 Jsoup로 진행해보자
   *
   *  이미지 비전은 왜쓰는거지? -> 이미지를 뭐 분석한다 이건 것 같은데, 이미지만 넣어주는게 맞나?
   *  어떻게 더 좋고 효율적인 응답을 받을 수 있을까?
   *  일단 softDelete부터 해결하고 돌아오데,
   */
  ClothesDto extractImageAndBasicInfo(String url, Long ownerId);

  // JSON-LD는 뭔데?

}
