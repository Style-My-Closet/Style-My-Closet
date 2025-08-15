package com.stylemycloset.testconfig;

import com.stylemycloset.binarycontent.service.ImageStoragePort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import com.stylemycloset.binarycontent.storage.BinaryContentStorage;
import com.stylemycloset.binarycontent.repository.BinaryContentRepository;
import com.stylemycloset.binarycontent.service.BinaryContentService;
// 핵심 Bean들만 유지
import java.util.concurrent.CompletableFuture;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

import java.util.HashMap;

@TestConfiguration
@Profile("test")
public class TestImageStorageConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @Primary
    public ImageStoragePort mockImageStoragePort() {
        return new ImageStoragePort() {
            @Override
            public UploadResult upload(byte[] data, String objectKey, String contentType) {
                // 테스트용 Mock 구현
                return new UploadResult(objectKey, "http://test-storage/" + objectKey);
            }

            @Override
            public PresignedUrl presignPut(String objectKey, String contentType, long expirationSeconds) {
                // 테스트용 Mock 구현
                return new PresignedUrl(objectKey, "http://test-presigned/" + objectKey, new HashMap<>());
            }

            @Override
            public String publicUrl(String objectKey) {
                // 테스트용 Mock 구현
                return "http://test-storage/" + objectKey;
            }
        };
    }

    // EntityManager는 실제 Bean을 사용하도록 제거

    @Bean
    @Primary
    public DispatcherServletPath dispatcherServletPath() {
        return () -> "/";
    }

    @Bean
    @Primary
    public ServletWebServerFactory servletWebServerFactory() {
        return new TomcatServletWebServerFactory();
    }

    @Bean
    @Primary
    public BinaryContentStorage mockBinaryContentStorage() {
        return new BinaryContentStorage() {
            @Override
            public CompletableFuture<UUID> put(UUID binaryContentId, byte[] bytes) {
                return CompletableFuture.completedFuture(binaryContentId);
            }

            @Override
            public InputStream get(UUID binaryContentId) {
                return null; // Mock implementation
            }

            @Override
            public URL getUrl(UUID binaryContentId) {
                try {
                    return new URL("http://test-storage/" + binaryContentId);
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }

   
}
