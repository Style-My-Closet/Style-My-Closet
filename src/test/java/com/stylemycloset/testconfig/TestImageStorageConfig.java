package com.stylemycloset.testconfig;

import com.stylemycloset.binarycontent.service.ImageStoragePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

@TestConfiguration
public class TestImageStorageConfig {

    @Bean
    @Primary
    public ImageStoragePort testImageStoragePort() {
        return new MockImageStoragePort();
    }

    public static class MockImageStoragePort implements ImageStoragePort {

        @Override
        public UploadResult upload(byte[] data, String objectKey, String contentType) {
            // 테스트용 가짜 URL 반환
            String fakeUrl = "http://test-storage.com/" + objectKey;
            return new UploadResult(objectKey, fakeUrl);
        }

        @Override
        public PresignedUrl presignPut(String objectKey, String contentType, long expirationSeconds) {
            // 테스트용 가짜 presigned URL 반환
            String fakePresignedUrl = "http://test-storage.com/presigned/" + objectKey;
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);
            return new PresignedUrl(objectKey, fakePresignedUrl, headers);
        }

        @Override
        public String publicUrl(String objectKey) {
            // 테스트용 가짜 public URL 반환
            return "http://test-storage.com/public/" + objectKey;
        }
    }
}
