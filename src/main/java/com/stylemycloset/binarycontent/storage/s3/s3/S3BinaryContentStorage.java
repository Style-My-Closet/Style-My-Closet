package com.stylemycloset.binarycontent.storage.s3.s3;

import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.binarycontent.storage.s3.BinaryContentStorage;
import com.stylemycloset.binarycontent.storage.s3.s3.exception.S3UploadArgumentException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Component
@RequiredArgsConstructor
public class S3BinaryContentStorage implements BinaryContentStorage {

  private static final int MAX_ATTEMPT = 3;

  private final S3Client s3Client;
  private final S3Presigner s3Presigner;

  @Value("${style-my-closet.storage.s3.presigned-url-expiration}")
  private long presignedUrlExpirationSeconds;
  @Value("${style-my-closet.storage.s3.bucket}")
  private String bucket;

  @Retryable(
      retryFor = {AwsServiceException.class, SdkClientException.class},
      notRecoverable = S3UploadArgumentException.class,
      maxAttempts = MAX_ATTEMPT,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Override
  public UUID put(UUID binaryContentId, byte[] bytes) {
    validatePutArgument(binaryContentId, bytes);

    String key = binaryContentId.toString();
    PutObjectRequest putRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(MediaType.IMAGE_JPEG_VALUE)
        .build();
    s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

    return binaryContentId;
  }

  @Retryable(
      retryFor = {AwsServiceException.class, SdkClientException.class},
      notRecoverable = S3UploadArgumentException.class,
      maxAttempts = MAX_ATTEMPT,
      backoff = @Backoff(delay = 1000, multiplier = 2.0)
  )
  @Async("uploadExecutor")
  @Override
  public CompletableFuture<UUID> putAsync(UUID binaryContentId, byte[] bytes) {
    validatePutArgument(binaryContentId, bytes);

    String key = binaryContentId.toString();
    PutObjectRequest putRequest = PutObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .contentType(MediaType.IMAGE_JPEG_VALUE)
        .build();
    s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

    return CompletableFuture.completedFuture(binaryContentId);
  }

  @Override
  public InputStream get(UUID binaryContentId) {
    if (binaryContentId == null) {
      return null;
    }
    String key = binaryContentId.toString();
    GetObjectRequest getRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    return s3Client.getObject(getRequest);
  }

  @Override
  public URL getUrl(UUID binaryContentId) {
    if (binaryContentId == null) {
      return null;
    }

    String key = binaryContentId.toString();
    GetObjectRequest getObjectRequest = GetObjectRequest.builder()
        .bucket(bucket)
        .key(key)
        .build();

    PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(
        builder -> builder.getObjectRequest(getObjectRequest)
            .signatureDuration(Duration.ofSeconds(presignedUrlExpirationSeconds))
    );
    return presignedGetObjectRequest.url();
  }

  private void validatePutArgument(UUID binaryContentId, byte[] bytes) {
    if (binaryContentId == null || bytes == null || bytes.length == 0) {
      throw new S3UploadArgumentException();
    }
  }

}
