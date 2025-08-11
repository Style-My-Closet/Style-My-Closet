package com.stylemycloset.binarycontent.service;

import com.stylemycloset.binarycontent.BinaryContent;
import com.stylemycloset.binarycontent.BinaryContentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDownloadService {
    
    private final BinaryContentRepository binaryContentRepository;
    private final RestTemplate restTemplate;
    
    @Value("${app.image.storage.path:storage/images}")
    private String baseStoragePath;

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
                "temp", // 임시 파일명
                null,
                contentType, 
                (long) imageData.length
            );
            binaryContent = binaryContentRepository.save(binaryContent);
            
            String extension = getFileExtensionFromFilename(file.getOriginalFilename());
            String finalFileName = binaryContent.getId().toString() + "." + extension;
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String finalLocalPath = saveToLocalFile(imageData, finalFileName, dateFolder);
            String publicUrl = "/files/images/" + dateFolder + "/" + finalFileName;
            
            // 4. 파일명 및 공개 URL 업데이트
            binaryContent.updateFileInfo(finalLocalPath, publicUrl);
            binaryContent = binaryContentRepository.save(binaryContent);
            
            return binaryContent;
            
        } catch (Exception e) {
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
                "temp", // 임시 파일명
                imageUrl, // 원본 이미지 URL 저장
                contentType, 
                (long) imageData.length
            );
            binaryContent = binaryContentRepository.save(binaryContent);
            
            String extension = getFileExtensionFromUrl(imageUrl);
            String finalFileName = binaryContent.getId().toString() + "." + extension;
            String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String finalLocalPath = saveToLocalFile(imageData, finalFileName, dateFolder);
            String publicUrl = "/files/images/" + dateFolder + "/" + finalFileName;
            
            // 4. 파일명 업데이트 (로컬 경로 + 공개 URL)
            binaryContent.updateFileInfo(finalLocalPath, publicUrl);
            binaryContent = binaryContentRepository.save(binaryContent);
            
            return binaryContent;
            
        } catch (Exception e) {
            BinaryContent failedContent = new BinaryContent(
                "FAILED_" + generateFileName(imageUrl),
                imageUrl, // 원본 URL은 저장
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
    

    private String saveToLocalFile(byte[] imageData, String fileName, String dateFolder) throws IOException {
        Path directoryPath = Paths.get(baseStoragePath, dateFolder);
        Files.createDirectories(directoryPath);
        
        Path filePath = directoryPath.resolve(fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageData);
        }
        
        return filePath.toString();
    }
    
    private String generateFileName(String imageUrl) {
        String extension = getFileExtensionFromUrl(imageUrl);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }
    

    private String getFileExtensionFromUrl(String imageUrl) {
        try {
            String path = new URL(imageUrl).getPath();
            int lastDotIndex = path.lastIndexOf('.');
            if (lastDotIndex > 0 && lastDotIndex < path.length() - 1) {
                return path.substring(lastDotIndex + 1).toLowerCase();
            }
        } catch (Exception e) {
            log.debug("URL에서 확장자 추출 실패: {}", imageUrl);
        }
        return "jpg";
    }
    

    private String getFileExtensionFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg";
    }

    private String getContentTypeFromUrl(String imageUrl) {
        String extension = getFileExtensionFromUrl(imageUrl);
        return switch (extension.toLowerCase()) {
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            default -> "image/jpeg";
        };
    }
    

}