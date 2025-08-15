package com.stylemycloset.cloth.controller;

import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.service.ClothProductExtractionService;
import com.stylemycloset.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
@Slf4j
public class ClothingController {

    private final ClothService clothService;
    private final ClothProductExtractionService clothProductExtractionService;

    @GetMapping
    public ResponseEntity<ClothListResponseDto> getClothes(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                           CursorDto cursorDto) {

        ClothListResponseDto response = clothService.getClothesWithCursor(userId, cursorDto);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ClothResponseDto> createCloth(@AuthenticationPrincipal(expression = "userId") Long userId,
                                                        @RequestPart("request") ClothCreateRequestDto request,
                                                        @RequestPart(value = "image", required = false) MultipartFile image) {

        ClothResponseDto response = clothService.createClothWithImage(request, image, userId);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{clothesId}")
    public ResponseEntity<Void> deleteCloth(@PathVariable Long clothesId) {
        clothService.deleteCloth(clothesId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{clothesId}")
    public ResponseEntity<ClothUpdateResponseDto> updateCloth(
            @PathVariable Long clothesId,
            @RequestPart("request") ClothUpdateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        ClothUpdateResponseDto response = clothService.updateCloth(clothesId, request, image);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/extractions")
    public ResponseEntity<ClothResponseDto> extractClothInfo(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestParam("productUrl") String productUrl) {
        ClothResponseDto result = clothProductExtractionService.extractAndSave(productUrl, userId);
        return ResponseEntity.ok(result);
    }



}
