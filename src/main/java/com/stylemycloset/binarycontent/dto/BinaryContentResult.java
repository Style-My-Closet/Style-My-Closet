package com.stylemycloset.binarycontent.dto;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import java.time.Instant;
import java.util.UUID;

public record BinaryContentResult(
    UUID id,
    Instant createdAt,
    String originalFileName,
    String contentType
) {

  public static BinaryContentResult from(BinaryContent binaryContent) {
    return new BinaryContentResult(
        binaryContent.getId(),
        binaryContent.getCreatedAt(),
        binaryContent.getOriginalFileName(),
        binaryContent.getContentType()
    );
  }

}