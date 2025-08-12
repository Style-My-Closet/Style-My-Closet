package com.stylemycloset.binarycontent.service;

import com.stylemycloset.binarycontent.dto.BinaryContentRequest;
import com.stylemycloset.binarycontent.dto.BinaryContentResult;
import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

  BinaryContentResult createBinaryContent(BinaryContentRequest binaryContentRequest);

  BinaryContentResult getById(UUID id);

  List<BinaryContentResult> getByIdIn(List<UUID> ids);

  void delete(UUID id);

}
