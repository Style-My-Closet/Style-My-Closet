package com.stylemycloset.binarycontent.storage;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BinaryContentStorage {

  CompletableFuture<UUID> put(UUID binaryContentId, byte[] bytes);

  InputStream get(UUID binaryContentId);

  // 추후에 프론트 엔트 코드 보고 수정
  URL getUrl(UUID binaryContentId);

}
