package com.stylemycloset.clothes.service.attribute.impl;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.entity.attribute.QClothesAttributeDefinition;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectableRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectedRepository;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.hibernate.query.SortDirection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClothAttributeServiceImplTest extends IntegrationTestSupport {

  @Autowired
  private ClothesAttributeDefinitionRepository attributeDefinitionRepository;
  @Autowired
  private ClothesAttributeDefinitionSelectedRepository attributeDefinitionSelectedRepository;
  @Autowired
  private ClothesAttributeDefinitionSelectableRepository definitionSelectableRepository;

  @Autowired
  private ClothAttributeService clothAttributeService;

  @BeforeEach
  void setUp() {
    attributeDefinitionRepository.deleteAllInBatch();
    attributeDefinitionSelectedRepository.deleteAllInBatch();
    definitionSelectableRepository.deleteAllInBatch();
  }

  @DisplayName("속성 정의 이름을 바탕으로 정렬하고 오름 차순 입니다.")
  @Test
  void createAttribute() {
    // given
    ClothesAttributeDefinitionDto firstRequest = clothAttributeService.createAttribute(
        new ClothesAttributeCreateRequest(
            "차", List.of("랜드로바")
        )
    );
    ClothesAttributeDefinitionDto secondRequest = clothAttributeService.createAttribute(
        new ClothesAttributeCreateRequest(
            "가나", List.of("초콜릿")
        )
    );

    // when
    ClothesAttributeDefinitionDtoCursorResponse attributes = clothAttributeService.getAttributes(
        new ClothesAttributeSearchCondition(
            null,
            null,
            20,
            QClothesAttributeDefinition.clothesAttributeDefinition.name.getMetadata().getName(),
            SortDirection.ASCENDING.name(),
            null
        )
    );

    // then
    Assertions.assertThat(attributes.data())
        .extracting(ClothesAttributeDefinitionDto::definitionName)
        .containsExactly("가나", "차");
  }

}