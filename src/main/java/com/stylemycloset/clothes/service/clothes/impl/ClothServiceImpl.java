package com.stylemycloset.clothes.service.clothes.impl;

import static com.stylemycloset.common.config.RedisConfig.CLOTHES_CACHE;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.request.ClothBinaryContentRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothUpdateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesCreateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesSearchCondition;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.entity.clothes.Clothes;
import com.stylemycloset.clothes.exception.ClothesNotFoundException;
import com.stylemycloset.clothes.mapper.ClothesMapper;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.clothes.service.clothes.ClothService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

  private final ClothesRepository clothesRepository;
  private final ClothesAttributeSelectableService clothesAttributeSelectableService;
  private final ClothesBinaryContentService clothesBinaryContentService;
  private final ClothesMapper clothesMapper;

  @CacheEvict(value = CLOTHES_CACHE, allEntries = true)
  @Transactional
  @Override
  public ClothesDto createCloth(
      ClothesCreateRequest createRequest,
      ClothBinaryContentRequest imageRequest
  ) {
    List<ClothesAttributeSelectableValue> selectableValues = clothesAttributeSelectableService.getSelectableValues(
        createRequest.attributes()
    );
    BinaryContent image = clothesBinaryContentService.createBinaryContent(imageRequest);
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

  @Cacheable(
      value = CLOTHES_CACHE,
      key = "#p0.ownerId()+'_'+#p0.typeEqual()+'_'+#p0.limit()",
      condition = "#p0.cursor() == null && #p0.idAfter() == null"
  )
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

  @CacheEvict(value = CLOTHES_CACHE, allEntries = true)
  @Transactional
  @Override
  public ClothesDto updateCloth(
      Long clothId,
      ClothUpdateRequest updateRequest,
      ClothBinaryContentRequest imageRequest
  ) {
    Clothes cloth = getClothesById(clothId);
    List<ClothesAttributeSelectableValue> selectableValues = clothesAttributeSelectableService.getSelectableValues(
        updateRequest.attributes()
    );
    BinaryContent newImage = clothesBinaryContentService.createBinaryContent(imageRequest);
    cloth.update(updateRequest.name(), newImage, updateRequest.type(), selectableValues);
    Clothes savedClothes = clothesRepository.save(cloth);

    return clothesMapper.toResponse(savedClothes);
  }

  @CacheEvict(value = CLOTHES_CACHE, allEntries = true)
  @Transactional
  @Override
  public void softDeleteCloth(Long clothId) {
    Clothes clothes = getClothesById(clothId);
    clothes.softDelete();
    clothesRepository.save(clothes);
  }

  @CacheEvict(value = CLOTHES_CACHE, allEntries = true)
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
    throw new ClothesNotFoundException();
  }

  private Clothes getClothesById(Long clothId) {
    return clothesRepository.findById(clothId)
        .orElseThrow(ClothesNotFoundException::new);
  }

}

