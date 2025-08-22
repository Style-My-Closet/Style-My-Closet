package com.stylemycloset.clothes.service.clothes.impl;

import static com.stylemycloset.clothes.service.clothes.impl.parser.JsoupSelectorConstant.META_ATTRIBUTE_CONTENT;
import static com.stylemycloset.clothes.service.clothes.impl.parser.JsoupSelectorConstant.META_ATTRIBUTE_PROPERTY;
import static com.stylemycloset.clothes.service.clothes.impl.parser.JsoupSelectorConstant.META_PROPERTY_SELECTOR;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.clothes.dto.ClothesExtractedMetaInfo;
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
import com.stylemycloset.clothes.exception.ClothesExtractionFailedException;
import com.stylemycloset.clothes.exception.InvalidClothesMetaInfoException;
import com.stylemycloset.clothes.mapper.ClothesMapper;
import com.stylemycloset.clothes.repository.clothes.ClothesRepository;
import com.stylemycloset.clothes.service.clothes.impl.parser.ClothesUrlParser;
import com.stylemycloset.clothes.service.clothes.ClothService;
import com.stylemycloset.common.exception.ErrorCode;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.parser.StreamParser;
import org.springframework.data.domain.Slice;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClothServiceImpl implements ClothService {

  private final ClothesRepository clothesRepository;
  private final ClothesAttributeSelectableService clothesAttributeSelectableService;
  private final ClothesBinaryContentService clothesBinaryContentService;
  private final ClothesUrlParser clothesURLParser;
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

  @Retryable(
      retryFor = UncheckedIOException.class,
      maxAttempts = 3,
      backoff = @Backoff(
          delay = 1000,
          multiplier = 2.0
      )
  )
  @Override
  public ClothesDto extractInfo(String url) {
    try (StreamParser streamer = Jsoup.connect(url)
        .timeout(10_000)
        .execute()
        .streamParser()
    ) {
      ClothesExtractedMetaInfo metaInfo = clothesURLParser.extract(
          streamer,
          META_PROPERTY_SELECTOR,
          META_ATTRIBUTE_PROPERTY,
          META_ATTRIBUTE_CONTENT
      );

      validateParsedInfo(url, metaInfo);
      return ClothesDto.of(metaInfo.productName(), metaInfo.imageUrl());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Recover
  public ClothesDto recoverExtractInfo(UncheckedIOException uncheckedIOException, String url) {
    throw new ClothesExtractionFailedException();
  }

  private void validateParsedInfo(String url, ClothesExtractedMetaInfo metaInfo) {
    if (metaInfo == null || metaInfo.productName() == null || metaInfo.imageUrl() == null) {
      throw new InvalidClothesMetaInfoException()
          .addDetails("request_url", url);
    }
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

