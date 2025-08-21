package com.stylemycloset.binarycontent.service.impl;

import com.stylemycloset.binarycontent.dto.BinaryContentRequest;
import com.stylemycloset.binarycontent.dto.BinaryContentResult;
import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.binarycontent.exception.BinaryContentNotFoundException;
import com.stylemycloset.binarycontent.repository.BinaryContentRepository;
import com.stylemycloset.binarycontent.service.BinaryContentService;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@RequiredArgsConstructor
public class BinaryContentServiceImpl implements BinaryContentService {

  private final BinaryContentStorage binaryContentStorage;
  private final BinaryContentRepository binaryContentRepository;

  @Transactional
  @Override
  public BinaryContentResult createBinaryContent(BinaryContentRequest binaryContentRequest) {
    if (binaryContentRequest == null) {
      return null;
    }
    BinaryContent binaryContent = new BinaryContent(
        binaryContentRequest.fileName(),
        binaryContentRequest.contentType(),
        binaryContentRequest.size()
    );
    BinaryContent savedBinaryContent = binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(savedBinaryContent.getId(), binaryContentRequest.bytes());

    return BinaryContentResult.from(savedBinaryContent);
  }

  @Transactional(readOnly = true)
  @Override
  public BinaryContentResult getById(UUID id) {
    BinaryContent binaryContent = binaryContentRepository.findById(id)
        .orElseThrow(BinaryContentNotFoundException::new);

    return BinaryContentResult.from(binaryContent);
  }

  @Transactional(readOnly = true)
  @Override
  public List<BinaryContentResult> getByIdIn(List<UUID> ids) {
    return binaryContentRepository.findAllById(ids)
        .stream()
        .map(BinaryContentResult::from)
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
    throw new BinaryContentNotFoundException();
  }

}
