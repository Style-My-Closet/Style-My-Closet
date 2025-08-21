package com.stylemycloset.binarycontent.storage.s3;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface BinaryContentStorage {

  CompletableFuture<UUID> put(UUID binaryContentId, byte[] bytes);

  InputStream get(UUID binaryContentId);

  URL getUrl(UUID binaryContentId);

}
