package com.stylemycloset.binarycontent.service;

import java.util.Map;

public interface ImageStoragePort {

    record UploadResult(String objectKey, String publicUrl) {}
    record PresignedUrl(String objectKey, String url, Map<String, String> headers) {}

    UploadResult upload(byte[] data, String objectKey, String contentType);

    PresignedUrl presignPut(String objectKey, String contentType, long expirationSeconds);

    String publicUrl(String objectKey);
}


