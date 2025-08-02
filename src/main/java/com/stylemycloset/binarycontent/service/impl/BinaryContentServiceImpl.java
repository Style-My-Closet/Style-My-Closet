package com.stylemycloset.binarycontent.service.impl;

import com.stylemycloset.binarycontent.dto.BinaryContentRequest;
import com.stylemycloset.binarycontent.dto.BinaryContentResult;
import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.binarycontent.exception.BinaryContentNotFoundException;
import com.stylemycloset.binarycontent.repository.BinaryContentRepository;
import com.stylemycloset.binarycontent.service.BinaryContentService;
import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BinaryContentServiceImpl implements BinaryContentService {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;

  @Transactional
  @Override
  public BinaryContentResult createBinaryContent(BinaryContentRequest binaryContentRequest) {
    BinaryContent binaryContent = new BinaryContent(
        binaryContentRequest.fileName(),
        binaryContentRequest.contentType(),
        binaryContentRequest.size()
    );
    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(savedBinaryContent.getId(), binaryContentRequest.bytes());

    return BinaryContentResult.fromEntity(savedBinaryContent);
  }

  @Transactional(readOnly = true)
  @Override
  public BinaryContentResult getById(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(() -> new BinaryContentNotFoundException(Map.of()));

    return BinaryContentResult.fromEntity(binaryContent);
  }

  @Transactional(readOnly = true)
  @Override
  public List<BinaryContentResult> getByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids)
        .stream()
        .map(BinaryContentResult::fromEntity)
        .toList();
  }

  @Transactional
  @Override
  public void delete(UUID id) {
    validateBinaryContentExist(id);

    binaryContentRepository.deleteById(id);
  }

  private void validateBinaryContentExist(UUID id) {
    if (binaryContentRepository.existsById(id)) {
      return;
    }
    throw new BinaryContentNotFoundException(Map.of());
  }

}
