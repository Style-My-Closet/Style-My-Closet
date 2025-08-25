package com.stylemycloset.clothes.service.attribute.impl;

import static com.stylemycloset.clothes.entity.attribute.QClothesAttributeDefinition.clothesAttributeDefinition;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeUpdateRequest;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectableRepository;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;

class ClothAttributeServiceImplTest extends IntegrationTestSupport {

  @Autowired
  private ClothesAttributeDefinitionRepository attributeDefinitionRepository;
  @Autowired
  private ClothesAttributeDefinitionSelectableRepository definitionSelectableRepository;

  @Autowired
  private ClothAttributeService clothAttributeService;

  @BeforeEach
  void setUp() {
    attributeDefinitionRepository.deleteAllInBatch();
    definitionSelectableRepository.deleteAllInBatch();
  }

  @DisplayName("속성 정의 이름을 바탕으로 정렬하고 오름 차순 입니다.")
  @Test
  void getAttributeAscName() {
    // given
    ClothesAttributeDefinitionDto laterInAscendingOrder = createAttributeWithSelectableValue(
        "차",
        List.of("랜드로바")
    );
    ClothesAttributeDefinitionDto earlierInAscendingOrder = createAttributeWithSelectableValue(
        "가나",
        List.of("초콜릿")
    );

    // when
    ClothesAttributeDefinitionDtoCursorResponse attributes = clothAttributeService.getAttributes(
        new ClothesAttributeSearchCondition(
            null,
            null,
            20,
            clothesAttributeDefinition.name.getMetadata().getName(),
            Direction.ASC,
            null
        )
    );

    // then
    Assertions.assertThat(attributes.data())
        .extracting(ClothesAttributeDefinitionDto::definitionName)
        .containsExactly(
            earlierInAscendingOrder.definitionName(),
            laterInAscendingOrder.definitionName()
        );
  }

  @DisplayName("선택가능한 의상 속성을 업데이트 합니다")
  @Test
  void testUpdateAttribute() {
    // given
    String definitionName = "계절";
    List<String> selectableValues = List.of("봄, 여름", "가을", "겨울");
    ClothesAttributeDefinitionDto attribute = createAttributeWithSelectableValue(
        definitionName, selectableValues);

    // when
    List<String> newSelectableValues = List.of("봄, 여름", "가을", "축구");
    ClothesAttributeUpdateRequest updateRequest = new ClothesAttributeUpdateRequest(
        definitionName,
        newSelectableValues
    );
    ClothesAttributeDefinitionDto updatedAttribute = clothAttributeService.updateAttribute(
        attribute.id(),
        updateRequest
    );

    // then
    Assertions.assertThat(updatedAttribute)
        .extracting(
            ClothesAttributeDefinitionDto::definitionName,
            ClothesAttributeDefinitionDto::selectableValues
        )
        .containsExactly(definitionName, newSelectableValues);
  }

  private ClothesAttributeDefinitionDto createAttributeWithSelectableValue(
      String definitionName,
      List<String> selectableValue
  ) {
    return clothAttributeService.createAttribute(
        new ClothesAttributeCreateRequest(definitionName, selectableValue)
    );
  }

}