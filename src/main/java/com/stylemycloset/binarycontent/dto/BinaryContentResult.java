package com.stylemycloset.binarycontent.dto;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BinaryContentResult(
    UUID id,
    Instant createdAt,
    String name,
    String contentType
) {

  public static BinaryContentResult fromEntity(BinaryContent binaryContent) {
    return new BinaryContentResult(
        binaryContent.getId(),
        binaryContent.getCreatedAt(),
        binaryContent.getOriginalFileName(),
        binaryContent.getContentType()
    );
  }

  public static List<BinaryContentResult> fromEntity(List<BinaryContent> binaryContents) {
    if (binaryContents == null) {
      return null;
    }

    return binaryContents.stream()
        .map(BinaryContentResult::fromEntity)
        .toList();
  }

}