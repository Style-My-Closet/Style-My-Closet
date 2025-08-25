package com.stylemycloset.clothes.service.attribute.impl;

import static com.stylemycloset.common.config.CacheConfig.CLOTHES_ATTRIBUTES_KEY;
import static com.stylemycloset.common.config.CacheConfig.CLOTHES_ATTRIBUTE_CACHE;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeUpdateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.exception.ClothesAttributeDefinitionDuplicateException;
import com.stylemycloset.clothes.exception.ClothesAttributeNotFoundException;
import com.stylemycloset.clothes.mapper.AttributeMapper;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClothAttributeServiceImpl implements ClothAttributeService {

  private final ClothesAttributeDefinitionRepository clothesAttributeDefinitionRepository;
  private final AttributeMapper attributeMapper;
  private final ApplicationEventPublisher eventPublisher;

  @CacheEvict(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional
  @Override
  public ClothesAttributeDefinitionDto createAttribute(
      ClothesAttributeCreateRequest request
  ) {
    validateAttributeDefinitionDuplicateName(request.name());
    ClothesAttributeDefinition attribute = new ClothesAttributeDefinition(
        request.name(),
        request.selectableValues()
    );
    ClothesAttributeDefinition savedAttribute = clothesAttributeDefinitionRepository.save(
        attribute);

    publishNewAttributeEvent(savedAttribute);
    return ClothesAttributeDefinitionDto.from(savedAttribute);
  }

  @Cacheable(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional(readOnly = true)
  @Override
  public ClothesAttributeDefinitionDtoCursorResponse getAttributes(
      ClothesAttributeSearchCondition attributeSearchCondition
  ) {
    Slice<ClothesAttributeDefinition> attributes = clothesAttributeDefinitionRepository.findWithCursorPagination(
        attributeSearchCondition.cursor(),
        attributeSearchCondition.idAfter(),
        attributeSearchCondition.limit(),
        attributeSearchCondition.sortBy(),
        attributeSearchCondition.sortDirection(),
        attributeSearchCondition.keywordLike()
    );

    return attributeMapper.toPageResponse(attributes);
  }

  @CacheEvict(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional
  @Override
  public ClothesAttributeDefinitionDto updateAttribute(
      Long attributeId,
      ClothesAttributeUpdateRequest request
  ) {
    validateAttributeDefinitionDuplicateName(request.name());
    ClothesAttributeDefinition attribute = getAttribute(attributeId);
    attribute.update(request.name(), request.selectableValues());
    ClothesAttributeDefinition savedAttribute = clothesAttributeDefinitionRepository.save(
        attribute);

    publishAttributeChangedEvent(savedAttribute);
    return ClothesAttributeDefinitionDto.from(savedAttribute);
  }

  @CacheEvict(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional
  @Override
  public void softDeleteAttributeById(Long definitionId) {
    ClothesAttributeDefinition attribute = getAttribute(definitionId);
    attribute.softDelete();
    clothesAttributeDefinitionRepository.save(attribute);
  }

  @CacheEvict(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional
  @Override
  public void deleteAttributeById(Long definitionId) {
    validateAttributeExist(definitionId);
    clothesAttributeDefinitionRepository.deleteById(definitionId);
  }

  private ClothesAttributeDefinition getAttribute(Long id) {
    return clothesAttributeDefinitionRepository.findByIdAndDeletedAtIsNull(id)
        .orElseThrow(ClothesAttributeNotFoundException::new);
  }

  private void publishAttributeChangedEvent(ClothesAttributeDefinition saved) {
    eventPublisher.publishEvent(
        new ClothAttributeChangedEvent(saved.getId(), saved.getName())
    );
  }

  private void publishNewAttributeEvent(ClothesAttributeDefinition saved) {
    eventPublisher.publishEvent(
        new NewClothAttributeEvent(saved.getId(), saved.getName())
    );
  }

  private void validateAttributeDefinitionDuplicateName(String name) {
    if (clothesAttributeDefinitionRepository.existsByActiveAttributeDefinition(name)) {
      throw new ClothesAttributeDefinitionDuplicateException();
    }
  }

  private void validateAttributeExist(Long attributeId) {
    if (clothesAttributeDefinitionRepository.existsById(attributeId)) {
      return;
    }
    throw new ClothesAttributeNotFoundException();
  }

} 