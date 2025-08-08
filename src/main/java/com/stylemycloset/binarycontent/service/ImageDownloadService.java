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
    
    /**
     * 이미지 URL 목록을 받아서 다운로드하고 메타데이터 저장
     */
    @Transactional
    public List<BinaryContent> downloadAndSaveImages(List<String> imageUrls) {
        log.info("이미지 다운로드 시작: {}개", imageUrls.size());
        
        return imageUrls.stream()
                .map(this::downloadSingleImage)
                .toList();
    }
    
    /**
     * MultipartFile을 BinaryContent로 저장 (이미지 업데이트용)
     */
    @Transactional
    public BinaryContent saveUploadedImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }
        
        log.info("업로드된 이미지 저장 시작: {}", file.getOriginalFilename());
        
        try {
            // 1. MultipartFile을 byte 배열로 변환
            byte[] imageData = file.getBytes();
            
            // 2. 먼저 BinaryContent 생성해서 ID 얻기
            String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
            BinaryContent binaryContent = new BinaryContent(
                "temp", // 임시 파일명
                null, // 업로드 파일은 원본 URL이 없음
                contentType, 
                (long) imageData.length
            );
            binaryContent = binaryContentRepository.save(binaryContent);
            
            // 3. ID를 사용해서 파일 저장
            String extension = getFileExtensionFromFilename(file.getOriginalFilename());
            String finalFileName = binaryContent.getId().toString() + "." + extension;
            String finalLocalPath = saveToLocalFile(imageData, finalFileName);
            
            // 4. 파일명 업데이트
            binaryContent.updateFileInfo(finalLocalPath, null); // 업로드 파일은 imageUrl이 null
            binaryContent = binaryContentRepository.save(binaryContent);
            
            log.info("업로드된 이미지 저장 성공: {} -> {}", file.getOriginalFilename(), finalLocalPath);
            return binaryContent;
            
        } catch (Exception e) {
            log.error("업로드된 이미지 저장 실패: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("이미지 저장 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 기존 이미지를 새 이미지로 업데이트 (소프트 딜리트)
     */
    @Transactional
    public BinaryContent updateImage(BinaryContent oldBinaryContent, MultipartFile newImageFile) {
        log.info("이미지 업데이트 시작: {} -> {}", 
            oldBinaryContent != null ? oldBinaryContent.getFileName() : "없음", 
            newImageFile.getOriginalFilename());
        
        // 1. 새 이미지 저장
        BinaryContent newBinaryContent = saveUploadedImage(newImageFile);
        
        // 2. 기존 이미지 소프트 딜리트
        if (oldBinaryContent != null) {
            oldBinaryContent.softDelete();
            binaryContentRepository.save(oldBinaryContent);
            log.info("기존 이미지 소프트 딜리트 완료: {}", oldBinaryContent.getFileName());
        }
        
        return newBinaryContent;
    }
    
    /**
     * 단일 이미지 다운로드 및 저장 (기존 BinaryContent 구조에 맞춤)
     */
    private BinaryContent downloadSingleImage(String imageUrl) {
        try {
            // 1. 실제 이미지 다운로드
            byte[] imageData = downloadImageData(imageUrl);
            
            // 2. 먼저 BinaryContent 생성해서 ID 얻기
            String contentType = getContentTypeFromUrl(imageUrl);
            BinaryContent binaryContent = new BinaryContent(
                "temp", // 임시 파일명 (나중에 업데이트)
                imageUrl, // 원본 이미지 URL 저장
                contentType, 
                (long) imageData.length
            );
            binaryContent = binaryContentRepository.save(binaryContent);
            
            // 3. ID를 사용해서 파일 저장
            String extension = getFileExtensionFromUrl(imageUrl);
            String finalFileName = binaryContent.getId().toString() + "." + extension;
            String finalLocalPath = saveToLocalFile(imageData, finalFileName);
            
            // 4. 파일명 업데이트 (로컬 경로로)
            binaryContent.updateFileInfo(finalLocalPath, imageUrl);
            binaryContent = binaryContentRepository.save(binaryContent);
            
            log.info("이미지 다운로드 성공: {} -> {}", imageUrl, finalLocalPath);
            return binaryContent;
            
        } catch (Exception e) {
            log.error("이미지 다운로드 실패: {}", imageUrl, e);
            // 실패한 경우도 기록 (크기 0으로)
            BinaryContent failedContent = new BinaryContent(
                "FAILED_" + generateFileName(imageUrl),
                imageUrl, // 원본 URL은 저장
                "image/jpeg",
                0L
            );
            return binaryContentRepository.save(failedContent);
        }
    }
    
    /**
     * 이미지 데이터 다운로드
     */
    private byte[] downloadImageData(String imageUrl) throws IOException {
        log.debug("이미지 다운로드 중: {}", imageUrl);
        
        try {
            // RestTemplate을 사용해서 이미지 데이터 가져오기
            return restTemplate.getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            // RestTemplate 실패 시 직접 URL 연결 시도
            log.warn("RestTemplate 실패, 직접 연결 시도: {}", imageUrl);
            try (var inputStream = new URL(imageUrl).openStream()) {
                return inputStream.readAllBytes();
            }
        }
    }
    
    /**
     * 로컬 파일에 저장
     */
    private String saveToLocalFile(byte[] imageData, String fileName) throws IOException {
        // 날짜별 디렉토리 구조: storage/images/2024/01/15/
        String dateFolder = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path directoryPath = Paths.get(baseStoragePath, dateFolder);
        
        // 디렉토리 생성
        Files.createDirectories(directoryPath);
        
        // 파일 저장
        Path filePath = directoryPath.resolve(fileName);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(imageData);
        }
        
        return filePath.toString();
    }
    
    /**
     * 고유한 파일명 생성
     */
    private String generateFileName(String imageUrl) {
        String extension = getFileExtensionFromUrl(imageUrl);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        
        return String.format("%s_%s.%s", timestamp, uuid, extension);
    }
    
    /**
     * URL에서 파일 확장자 추출
     */
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
        return "jpg"; // 기본값
    }
    
    /**
     * 파일명에서 확장자 추출
     */
    private String getFileExtensionFromFilename(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "jpg"; // 기본값
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1).toLowerCase();
        }
        return "jpg"; // 기본값
    }
    
    /**
     * URL에서 Content-Type 추정
     */
    private String getContentTypeFromUrl(String imageUrl) {
        String extension = getFileExtensionFromUrl(imageUrl);
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            default -> "image/jpeg";
        };
    }
    
    /**
     * 모든 이미지 조회
     */
    @Transactional(readOnly = true)
    public List<BinaryContent> findAllImages() {
        return binaryContentRepository.findAllByOrderByCreatedAtDesc();
    }
    
    /**
     * 성공적으로 다운로드된 이미지 조회 (size > 0)
     */
    @Transactional(readOnly = true)
    public List<BinaryContent> findSuccessfullyDownloadedImages() {
        return binaryContentRepository.findBySizeGreaterThanOrderByCreatedAtDesc(0L);
    }
}