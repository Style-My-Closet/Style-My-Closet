package com.stylemycloset.clothes.dto.clothes.request;

import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public record ClothBinaryContentRequest(
    String originalFileName,
    String contentType,
    Long size,
    byte[] bytes
) {

  public static ClothBinaryContentRequest from(
      MultipartFile image
  ) {
    if (image == null || image.isEmpty()) {
      return null;
    }

    return new ClothBinaryContentRequest(
        image.getOriginalFilename(),
        image.getContentType(),
        image.getSize(),
        convertToBytes(image)
    );
  }

  private static byte[] convertToBytes(MultipartFile image) {
    try {
      return image.getBytes();
    } catch (IOException e) {
      throw new IllegalArgumentException("멀티파트 파일 변화중 에러 발생");
    }
  }

}
