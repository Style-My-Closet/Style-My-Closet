package com.stylemycloset.clothes.repository.clothes.impl;

import com.stylemycloset.IntegrationTestSupport;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.request.AttributeRequestDto;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.clothes.AttributeDto;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.request.ClothesCreateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesSearchCondition;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.entity.clothes.ClothesType;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectableRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectedRepository;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import com.stylemycloset.clothes.service.clothes.ClothService;
import com.stylemycloset.user.entity.User;
import com.stylemycloset.user.repository.UserRepository;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ClothesServiceTest extends IntegrationTestSupport {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ClothesAttributeDefinitionRepository attributeDefinitionRepository;
  @Autowired
  private ClothesAttributeDefinitionSelectableRepository definitionSelectableRepository;
  @Autowired
  private ClothAttributeService clothAttributeService;

  @Autowired
  private ClothesAttributeDefinitionSelectedRepository clothesAttributeDefinitionSelectedRepository;
  @Autowired
  private ClothesAttributeDefinitionSelectableRepository clothesAttributeDefinitionSelectableRepository;
  @Autowired
  private ClothesRepository clothesRepository;
  @Autowired
  private ClothService clothService;

  @BeforeEach
  void setUp() {
    userRepository.deleteAllInBatch();

    attributeDefinitionRepository.deleteAllInBatch();
    definitionSelectableRepository.deleteAllInBatch();

    clothesAttributeDefinitionSelectedRepository.deleteAllInBatch();
    clothesAttributeDefinitionSelectableRepository.deleteAllInBatch();
    clothesRepository.deleteAllInBatch();
  }

  @DisplayName("옷 목록을 조회합니다.")
  @Test
  void getClothesTest() {
    // given
    User owner = save("name", "name@naver.com");
    ClothesAttributeDefinitionDto carAttribute = createAttributeWithSelectableValue(
        "차",
        List.of("랜드로바", "벤츠")
    );
    ClothesAttributeDefinitionDto countryAttribute = createAttributeWithSelectableValue(
        "가나",
        List.of("초콜릿", "달리기")
    );
    AttributeRequestDto carAttributeRequest = createAttributeRequest(carAttribute);
    AttributeRequestDto countryAttributeRequest = createAttributeRequest(countryAttribute);
    ClothesDto firstCloth = createClothes(owner, "옷", carAttributeRequest);
    ClothesDto secondCloth = createClothes(owner, "옷장사", countryAttributeRequest);

    // when
    ClothDtoCursorResponse clothes = clothService.getClothes(
        new ClothesSearchCondition(
            null,
            null,
            1,
            ClothesType.TOP,
            owner.getId()
        )
    );

    // then
    AttributeDto expectedAttribute = new AttributeDto(countryAttribute.id(),
        countryAttribute.definitionName(),
        countryAttribute.selectableValues(), countryAttributeRequest.value());
    Assertions.assertThat(clothes.data())
        .extracting(ClothesDto::name, ClothesDto::attributes)
        .containsExactly(Tuple.tuple(secondCloth.name(), List.of(expectedAttribute)));
  }

  @NotNull
  private AttributeRequestDto createAttributeRequest(
      ClothesAttributeDefinitionDto countryAttribute
  ) {
    return new AttributeRequestDto(
        countryAttribute.id(),
        countryAttribute.selectableValues().get(0)
    );
  }

  private ClothesDto createClothes(User owner, String clothesName,
      AttributeRequestDto countryAttributeRequest
  ) {
    return clothService.createCloth(
        new ClothesCreateRequest(
            owner.getId(),
            clothesName,
            ClothesType.TOP.name(),
            List.of(countryAttributeRequest)),
        null
    );
  }

  private User save(String name, String email) {
    return userRepository.save(new User(name, email, "p"));
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