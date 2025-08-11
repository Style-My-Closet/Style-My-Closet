package com.stylemycloset.cloth.controller;

import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.service.ClothProductExtractionService;
import com.stylemycloset.cloth.service.ClothService;
import com.stylemycloset.cloth.service.ImageVisionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothService clothService;
    private final ClothProductExtractionService clothProductExtractionService;

    @GetMapping
    public ResponseEntity<ClothListResponseDto> getClothes(@RequestHeader(name = "X-User-Id", required = false) Long userId,
                                                           CursorDto cursorDto) {
        Long effectiveUserId = (userId != null ? userId : 1L);
        ClothListResponseDto response = clothService.getClothesWithCursor(effectiveUserId, cursorDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ClothResponseDto> createCloth(@RequestHeader(name = "X-User-Id", required = false) Long userId,
                                                        @RequestBody ClothCreateRequestDto request) {
        Long effectiveUserId = (userId != null ? userId : 1L);
        ClothResponseDto response = clothService.createCloth(request, effectiveUserId);
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
            @RequestHeader(name = "X-User-Id", required = false) Long userId,
            @RequestParam("productUrl") String productUrl,
            @RequestParam(value = "mode", required = false, defaultValue = "full") String mode
    ) {
        Long effectiveUserId = (userId != null ? userId : 1L);
        ClothResponseDto result = clothProductExtractionService.extractAndSave(productUrl, effectiveUserId);
        return ResponseEntity.ok(result);
    }

}
