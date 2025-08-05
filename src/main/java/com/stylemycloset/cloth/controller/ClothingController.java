package com.stylemycloset.cloth.controller;

import com.stylemycloset.cloth.dto.ClothCreateRequestDto;
import com.stylemycloset.cloth.dto.ClothResponseDto;
import com.stylemycloset.cloth.dto.ClothUpdateRequestDto;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.ClothListResponseDto;
import com.stylemycloset.cloth.dto.response.ClothUpdateResponseDto;
import com.stylemycloset.cloth.service.ClothService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothingController {

    private final ClothService clothService;

    @GetMapping
    public ResponseEntity<ClothListResponseDto> getClothes(CursorDto cursorDto) {
        // 임시로 userId를 1L로 설정
        ClothListResponseDto response = clothService.getClothesWithCursor(1L, cursorDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ClothResponseDto> createCloth(
            @RequestPart("request") ClothCreateRequestDto request,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        // 임시로 userId를 1L로 설정
        ClothResponseDto response = clothService.createCloth(request, 1L, image);
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
    public ResponseEntity<Object> extractClothInfo(@RequestParam String purchaseLink) {
        //구현 예정
        return ResponseEntity.ok().build();
    }
}
