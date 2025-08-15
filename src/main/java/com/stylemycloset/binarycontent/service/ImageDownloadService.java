package com.stylemycloset.binarycontent.service;

import com.stylemycloset.binarycontent.repository.BinaryContentRepository;
import com.stylemycloset.binarycontent.entity.BinaryContent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDownloadService {
    
    private final BinaryContentRepository binaryContentRepository;
    private final RestTemplate restTemplate;
    private final ImageStoragePort imageStoragePort;
    
    @Value("${app.image.s3.prefix:images}")
    private String s3Prefix;

    @Transactional
    public List<BinaryContent> downloadAndSaveImages(List<String> imageUrls) {

        return imageUrls.stream()
                .map(this::downloadSingleImage)
                .toList();
    }
    

    @Transactional
    public BinaryContent saveUploadedImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }
        
        try {
            byte[] imageData = file.getBytes();
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            BinaryContent binaryContent = new BinaryContent(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload",
                contentType,
                (long) imageData.length
            );
            
            binaryContent = binaryContentRepository.save(binaryContent);

            // UUID ID를 직접 object key로 사용 (간단하고 유니크함)
            String objectKey = binaryContent.getId().toString();

            ImageStoragePort.UploadResult uploaded = imageStoragePort.upload(imageData, objectKey, contentType);
            binaryContent.updateImageUrl(uploaded.publicUrl());
            binaryContent = binaryContentRepository.save(binaryContent);

            return binaryContent;
            
        } catch (Exception e) {
            log.error("이미지 업로드 실패", e);
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }
    

    @Transactional
    public BinaryContent updateImage(BinaryContent oldBinaryContent, MultipartFile newImageFile) {

        
        BinaryContent newBinaryContent = saveUploadedImage(newImageFile);
        
        if (oldBinaryContent != null) {
            oldBinaryContent.softDelete();
            binaryContentRepository.save(oldBinaryContent);
        }
        
        return newBinaryContent;
    }
    

    private BinaryContent downloadSingleImage(String imageUrl) {
        try {
            byte[] imageData = downloadImageData(imageUrl);

            String contentType = getContentTypeFromUrl(imageUrl);
            BinaryContent binaryContent = new BinaryContent(
                generateFileName(imageUrl),
                contentType,
                (long) imageData.length
            );
            binaryContent = binaryContentRepository.save(binaryContent);

            // UUID ID를 직접 object key로 사용 (간단하고 유니크함)
            String objectKey = binaryContent.getId().toString();

            ImageStoragePort.UploadResult uploaded = imageStoragePort.upload(imageData, objectKey, contentType);
            binaryContent.updateImageUrl(uploaded.publicUrl());
            binaryContent = binaryContentRepository.save(binaryContent);

            return binaryContent;
            
        } catch (Exception e) {
            BinaryContent failedContent = new BinaryContent(
                "FAILED_" + generateFileName(imageUrl),
                "image/jpeg",
                0L
            );
            return binaryContentRepository.save(failedContent);
        }
    }
    

    private byte[] downloadImageData(String imageUrl) throws IOException {

        try {
            return restTemplate.getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            try (var inputStream = new URL(imageUrl).openStream()) {
                return inputStream.readAllBytes();
            }
        }
    }
    

    // 로컬 저장 로직 제거 (S3 사용)
    
    private String generateFileName(String imageUrl) {
        // UUID ID를 사용하므로 간단하게 처리
        return "downloaded_image";
    }

    private String getContentTypeFromUrl(String imageUrl) {
        // URL에서 확장자 추출을 시도하되, 실패하면 기본값 사용
        String extension = extractFileExtension(imageUrl);
        return mapExtensionToContentType(extension);
    }

    private String extractFileExtension(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "jpg";
        }
        
        try {
            // URL에서 경로 부분만 추출
            String path = new URL(imageUrl).getPath();
            int lastDotIndex = path.lastIndexOf('.');
            
            if (lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
                String extension = path.substring(lastDotIndex + 1).toLowerCase();
                // 알려진 이미지 확장자만 허용
                if (isValidImageExtension(extension)) {
                    return extension;
                }
            }
        } catch (Exception e) {
            log.debug("URL에서 확장자 추출 실패: {}, 기본값 jpg 사용", imageUrl);
        }
        
        return "jpg"; // 기본값
    }
    
    private boolean isValidImageExtension(String extension) {
        return extension.matches("^(jpg|jpeg|png|gif|webp|svg|bmp)$");
    }
    
    private String mapExtensionToContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "bmp" -> "image/bmp";
            case "jpeg" -> "image/jpeg";
            default -> "image/jpeg"; // jpg 포함
        };
    }

    // extensionFromContentType 메서드 제거됨 (UUID ID 사용으로 확장자 불필요)
    

}