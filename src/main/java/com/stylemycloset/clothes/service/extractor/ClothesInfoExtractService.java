package com.stylemycloset.clothes.service.extractor;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;

public interface ClothesInfoExtractService {

  ClothesDto extractInfo(String url);

}
