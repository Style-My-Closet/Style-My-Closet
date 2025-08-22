package com.stylemycloset.clothes.service.clothes;

import com.stylemycloset.clothes.dto.clothes.request.ClothBinaryContentRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesSearchCondition;
import com.stylemycloset.clothes.dto.clothes.request.ClothesCreateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothUpdateRequest;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.response.ClothUpdateResponseDto;

public interface ClothService {

  ClothesDto createCloth(
      ClothesCreateRequest clothCreateRequest,
      ClothBinaryContentRequest imageRequest
  );

  ClothDtoCursorResponse getClothes(ClothesSearchCondition clothesSearchCondition);

  ClothUpdateResponseDto updateCloth(
      Long clothId,
      ClothUpdateRequest requestDto,
      ClothBinaryContentRequest imageRequest
  );

  void softDeleteCloth(Long clothId);

  void deleteCloth(Long clothId);

  ClothesDto extractInfo(String url);

}
