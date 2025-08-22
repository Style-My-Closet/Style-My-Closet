package com.stylemycloset.clothes.service.extractor;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;

public interface ClothesInfoExtractionService {

  ClothesDto extractInfo(String url);

}
