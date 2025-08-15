package com.stylemycloset.testconfig;

import com.stylemycloset.binarycontent.service.ImageStoragePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestStorageConfig {

    @Bean
    @Primary
    public ImageStoragePort mockImageStoragePort() {
        return new ImageStoragePort() {
            @Override
            public UploadResult upload(byte[] data, String objectKey, String contentType) {
                return new UploadResult(objectKey, "http://test-storage.com/" + objectKey);
            }

            @Override
            public PresignedUrl presignPut(String objectKey, String contentType, long expirationSeconds) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", contentType);
                return new PresignedUrl(objectKey, "http://test-storage.com/presigned/" + objectKey, headers);
            }

            @Override
            public String publicUrl(String objectKey) {
                return "http://test-storage.com/public/" + objectKey;
            }
        };
    }
}
