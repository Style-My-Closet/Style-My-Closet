package com.stylemycloset.clothes.service.attribute.impl;

import static com.stylemycloset.common.config.CacheConfig.CLOTHES_ATTRIBUTES_KEY;
import static com.stylemycloset.common.config.CacheConfig.CLOTHES_ATTRIBUTE_CACHE;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeUpdateRequest;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeDefinition;
import com.stylemycloset.clothes.entity.attribute.ClothesAttributeSelectableValue;
import com.stylemycloset.clothes.exception.ClothesAttributeDefinitionDuplicateException;
import com.stylemycloset.clothes.exception.ClothesAttributeNotFoundException;
import com.stylemycloset.clothes.mapper.AttributeMapper;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionRepository;
import com.stylemycloset.clothes.repository.attribute.ClothesAttributeDefinitionSelectedRepository;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

  private final ClothesAttributeDefinitionSelectedRepository clothesAttributeDefinitionSelectedRepository;
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

  @Cacheable(
      value = CLOTHES_ATTRIBUTE_CACHE,
      key = CLOTHES_ATTRIBUTES_KEY,
      condition = "#p0.cursor() == null && #p0.idAfter() == null"
  )
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
    ClothesAttributeDefinition attribute = getAttributeDefinitionWithSelectables(attributeId);
    validateAttributeDefinitionDuplicateName(request.name(), attribute.getName());
    List<ClothesAttributeSelectableValue> oldSelectables = List.copyOf(
        attribute.getSelectableValues()
    );
    attribute.update(request.name(), request.selectableValues());
    ClothesAttributeDefinition savedAttribute = clothesAttributeDefinitionRepository.save(
        attribute
    );
    softDeleteClothesSelectedValues(oldSelectables, savedAttribute.getSelectableValues());

    publishAttributeChangedEvent(savedAttribute);

    return ClothesAttributeDefinitionDto.from(savedAttribute);
  }

  @CacheEvict(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional
  @Override
  public void softDeleteAttributeById(Long definitionId) {
    ClothesAttributeDefinition attribute = getAttributeDefinitionWithSelectables(definitionId);
    attribute.softDelete();
    clothesAttributeDefinitionRepository.save(attribute);
    clothesAttributeDefinitionSelectedRepository.softDeleteBySelectableValues(
        attribute.getSelectableValues()
    );
  }

  @CacheEvict(value = CLOTHES_ATTRIBUTE_CACHE, key = CLOTHES_ATTRIBUTES_KEY)
  @Transactional
  @Override
  public void deleteAttributeById(Long definitionId) {
    ClothesAttributeDefinition attribute = getAttributeDefinitionWithSelectables(definitionId);
    clothesAttributeDefinitionRepository.deleteById(attribute.getId());
    clothesAttributeDefinitionSelectedRepository.deleteBySelectableValues(
        attribute.getSelectableValues()
    );
  }

  private ClothesAttributeDefinition getAttributeDefinitionWithSelectables(Long id) {
    return clothesAttributeDefinitionRepository.findByIdFetchSelectableValues(id)
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

  private void validateAttributeDefinitionDuplicateName(String newName) {
    if (clothesAttributeDefinitionRepository.existsByAttributeDefinition(newName)) {
      throw new ClothesAttributeDefinitionDuplicateException();
    }
  }

  private void validateAttributeDefinitionDuplicateName(
      String newName,
      String currentAttributeDefinitionName
  ) {
    if (newName.equals(currentAttributeDefinitionName)) {
      return;
    }
    if (clothesAttributeDefinitionRepository.existsByAttributeDefinition(newName)) {
      throw new ClothesAttributeDefinitionDuplicateException();
    }
  }

  private void softDeleteClothesSelectedValues(
      List<ClothesAttributeSelectableValue> attributeSelectableValues,
      List<ClothesAttributeSelectableValue> savedAttributeSelectableValues
  ) {
    Set<ClothesAttributeSelectableValue> oldSelectables = new LinkedHashSet<>(
        attributeSelectableValues
    );
    Set<ClothesAttributeSelectableValue> newSelectable = new LinkedHashSet<>(
        savedAttributeSelectableValues
    );
    oldSelectables.removeIf(newSelectable::contains);
    clothesAttributeDefinitionSelectedRepository.softDeleteBySelectableValues(oldSelectables);
  }

} 