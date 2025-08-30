package com.stylemycloset.binarycontent.mapper;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinaryContentMapper {

  private final BinaryContentStorage binaryContentStorage;

  public String extractUrl(BinaryContent binaryContent) {
    if (binaryContent == null) {
      return null;
    }

    return binaryContentStorage.getUrl(binaryContent.getId())
        .toString();
  }

}
