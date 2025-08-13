package com.stylemycloset.cloth.controller;

import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.service.ClothProductExtractionService;
import com.stylemycloset.binarycontent.service.ImageDownloadService;
import com.stylemycloset.binarycontent.entity.BinaryContent;
import com.stylemycloset.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothService clothService;
    private final ClothProductExtractionService clothProductExtractionService;
    private final ImageDownloadService imageDownloadService;

    @GetMapping
    public ResponseEntity<ClothListResponseDto> getClothes(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                           CursorDto cursorDto) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ClothListResponseDto response = clothService.getClothesWithCursor(userId, cursorDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothResponseDto> createCloth(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                        @RequestPart("request") ClothCreateRequestDto request,
                                                        @RequestPart(value = "image", required = false) MultipartFile image) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (image != null && !image.isEmpty()) {
            BinaryContent saved = imageDownloadService.saveUploadedImage(image);
            if (saved != null) {
                request.setBinaryContentId(saved.getId());
            }
        }
        ClothResponseDto response = clothService.createCloth(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClothResponseDto> createClothJson(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                            @RequestBody ClothCreateRequestDto request) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ClothResponseDto response = clothService.createCloth(request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteCloth(@PathVariable Long clothesId) {
        clothService.deleteCloth(clothesId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{clothesId}")
    public ResponseEntity<ClothUpdateResponseDto> updateCloth(
            @PathVariable String clothesId,
            @RequestPart("request") ClothUpdateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        ClothUpdateResponseDto response = clothService.updateCloth(Long.valueOf(clothesId), request, image);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/extractions")
    public ResponseEntity<ClothResponseDto> extractClothInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam("productUrl") String productUrl) {
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        ClothResponseDto result = clothProductExtractionService.extractAndSave(productUrl, userId);
        return ResponseEntity.ok(result);
    }

    

}
