package com.stylemycloset.clothes.service.clothes.impl;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.binarycontent.repository.BinaryContentRepository;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import com.stylemycloset.clothes.dto.clothes.request.ClothBinaryContentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClothesBinaryContentService {

  private final BinaryContentRepository binaryContentRepository;
  private final BinaryContentStorage binaryContentStorage;

  public BinaryContent createBinaryContent(ClothBinaryContentRequest request) {
    if (request == null) {
      return null;
    }

    BinaryContent binaryContent = new BinaryContent(
        request.originalFileName(),
        request.contentType(),
        request.size()
    );
    BinaryContent saved = binaryContentRepository.save(binaryContent);
    binaryContentStorage.put(saved.getId(), request.bytes());

    return saved;
  }

}
