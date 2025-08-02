package com.stylemycloset.binarycontent.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.springframework.web.multipart.MultipartFile;

public record BinaryContentRequest(
    @NotBlank String fileName,
    @NotBlank String contentType,
    @NotBlank long size,
    @NotBlank byte[] bytes
) {

  public static BinaryContentRequest from(MultipartFile multipartFile) {
    return new BinaryContentRequest(
        multipartFile.getName(),
        multipartFile.getContentType(),
        multipartFile.getSize(),
        getBytesFromMultiPartFile(multipartFile)
    );
  }

  public static byte[] getBytesFromMultiPartFile(MultipartFile file) {
    validateMultipartFile(file);

    try {
      return file.getBytes();
    } catch (IOException e) {
      throw new UncheckedIOException("파일 바이트 변환에 실패했습니다.", e);
    }
  }

  private static void validateMultipartFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("파일이 비어있거나 null입니다.");
    }
  }

}
