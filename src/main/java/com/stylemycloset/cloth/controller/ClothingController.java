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
import com.stylemycloset.security.ClosetUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<ClothListResponseDto> getClothes(@AuthenticationPrincipal UserDetails principal,
                                                           CursorDto cursorDto) {
        Long effectiveUserId = (principal != null ? extractUserId(principal) : 1L);
        ClothListResponseDto response = clothService.getClothesWithCursor(effectiveUserId, cursorDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClothResponseDto> createCloth(@AuthenticationPrincipal UserDetails principal,
                                                        @RequestPart("request") ClothCreateRequestDto request,
                                                        @RequestPart(value = "image", required = false) MultipartFile image) {
        Long effectiveUserId = (principal != null ? extractUserId(principal) : 1L);
        if (image != null && !image.isEmpty()) {
            BinaryContent saved = imageDownloadService.saveUploadedImage(image);
            if (saved != null) {
                request.setBinaryContentId(saved.getId());
            }
        }
        ClothResponseDto response = clothService.createCloth(request, effectiveUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ClothResponseDto> createClothJson(@AuthenticationPrincipal UserDetails principal,
                                                            @RequestBody ClothCreateRequestDto request) {
        Long effectiveUserId = (principal != null ? extractUserId(principal) : 1L);
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
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam("productUrl") String productUrl,
            @RequestParam(value = "mode", required = false, defaultValue = "full") String mode
    ) {
        Long effectiveUserId = (principal != null ? extractUserId(principal) : 1L);
        ClothResponseDto result = clothProductExtractionService.extractAndSave(productUrl, effectiveUserId);
        return ResponseEntity.ok(result);
    }

    private Long extractUserId(UserDetails principal) {
        if (principal instanceof ClosetUserDetails cud && cud.getUserId() != null) {
            return cud.getUserId();
        }
        try {
            return Long.parseLong(principal.getUsername());
        } catch (Exception ignore) {
            return 1L;
        }
    }

}
