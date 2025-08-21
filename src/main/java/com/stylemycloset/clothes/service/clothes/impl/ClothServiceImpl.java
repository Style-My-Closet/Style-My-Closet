package com.stylemycloset.clothes.service.clothes.impl;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.clothes.dto.clothes.request.ClothBinaryContentRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesSearchCondition;
import com.stylemycloset.clothes.dto.clothes.request.ClothUpdateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesCreateRequest;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.response.ClothUpdateResponseDto;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.exception.ClothesException;
import com.stylemycloset.clothes.mapper.ClothesMapper;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectedRepository;
import com.stylemycloset.clothes.service.clothes.ClothService;
import com.stylemycloset.common.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

  private final ClothesAttributeDefinitionSelectedRepository selectedValuesRepository;
  private final ClothesRepository clothesRepository;
  private final ClothesAttributeSelectableService clothesAttributeSelectableService;
  private final ClothesBinaryContentService clothesBinaryContentService;
  private final ClothesMapper clothesMapper;

  @Transactional
  @Override
  public ClothesDto createCloth(
      ClothesCreateRequest createRequest,
      ClothBinaryContentRequest imageRequest
  ) {
    BinaryContent image = clothesBinaryContentService.createBinaryContent(imageRequest);
    List<ClothesAttributeSelectableValue> selectableValues = clothesAttributeSelectableService.getSelectableValues(
        createRequest.attributes()
    );
    Clothes clothes = new Clothes(
        createRequest.ownerId(),
        createRequest.name(),
        image,
        createRequest.type(),
        selectableValues
    );
    Clothes savedClothes = clothesRepository.save(clothes);

    return clothesMapper.toResponse(savedClothes);
  }

  @Transactional(readOnly = true)
  @Override
  public ClothDtoCursorResponse getClothes(ClothesSearchCondition condition) {
    Slice<Clothes> clothes = clothesRepository.findClothesByCondition(
        condition.cursor(),
        condition.idAfter(),
        condition.limit(),
        condition.typeEqual(),
        condition.ownerId(),
        null,
        null
    );

    return clothesMapper.toPageResponse(clothes);
  }

  @Transactional
  @Override
  public ClothUpdateResponseDto updateCloth(
      Long clothId,
      ClothUpdateRequest updateRequest,
      ClothBinaryContentRequest imageRequest
  ) {
    Clothes cloth = getClothesById(clothId);
    BinaryContent image = clothesBinaryContentService.createBinaryContent(imageRequest);
    List<ClothesAttributeSelectableValue> selectableValues = clothesAttributeSelectableService.getSelectableValues(
        updateRequest.attributes()
    );
    cloth.update(updateRequest.name(), image, updateRequest.type(), selectableValues);

    Clothes savedCloth = clothesRepository.save(cloth);
    return ClothUpdateResponseDto.from(savedCloth);
  }

  @Transactional
  @Override
  public void softDeleteCloth(Long clothId) {
    Clothes clothes = getClothesById(clothId);
    clothes.softDelete();
    clothesRepository.save(clothes);
  }

  @Transactional
  @Override
  public void deleteCloth(Long clothId) {
    validateClothesExists(clothId);
    clothesRepository.deleteById(clothId);
  }

  private void validateClothesExists(Long clothId) {
    if (clothesRepository.existsById(clothId)) {
      return;
    }
    throw new ClothesException(ErrorCode.CLOTH_NOT_FOUND);
  }

  private Clothes getClothesById(Long clothId) {
    return clothesRepository.findById(clothId)
        .orElseThrow(() -> new ClothesException(ErrorCode.CLOTH_NOT_FOUND));
  }

}

