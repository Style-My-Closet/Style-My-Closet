package com.stylemycloset.binarycontent.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ImageStorageService implements ImageStoragePort {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${style-my-closet.storage.s3.bucket-name}")
    private String bucket;

    @Value("${style-my-closet.storage.s3.region}")
    private String region;

    @Value("${style-my-closet.storage.s3.public-base-url:https://%s.s3.%s.amazonaws.com}")
    private String publicBaseUrlFormat;

    @Override
    public UploadResult upload(byte[] data, String objectKey, String contentType) {
        try {
            // common 패키지의 S3Config에서 주입받은 클라이언트 사용
            PutObjectRequest put = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();
            s3Client.putObject(put, RequestBody.fromBytes(data));
            String publicUrl = String.format(publicBaseUrlFormat, bucket, region) + "/" + objectKey;
            return new UploadResult(objectKey, publicUrl);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public PresignedUrl presignPut(String objectKey, String contentType, long expirationSeconds) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expirationSeconds))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

            Map<String, List<String>> signed = presigned.signedHeaders();
            Map<String, String> flattened = new HashMap<>();
            if (signed != null) {
                for (Map.Entry<String, List<String>> e : signed.entrySet()) {
                    String value = (e.getValue() == null || e.getValue().isEmpty()) ? "" : String.join(",", e.getValue());
                    flattened.put(e.getKey(), value);
                }
            }

            return new PresignedUrl(objectKey, presigned.url().toString(), flattened);
        } catch (Exception e) {

            throw e;
        }
    }

    @Override
    public String publicUrl(String objectKey) {
        return String.format(publicBaseUrlFormat, bucket, region) + "/" + objectKey;
    }
}


