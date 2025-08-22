package com.stylemycloset.clothes.controller;

import com.stylemycloset.clothes.dto.clothes.ClothesDto;
import com.stylemycloset.clothes.dto.clothes.request.ClothBinaryContentRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothUpdateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesCreateRequest;
import com.stylemycloset.clothes.dto.clothes.request.ClothesSearchCondition;
import com.stylemycloset.clothes.dto.clothes.response.ClothDtoCursorResponse;
import com.stylemycloset.clothes.dto.clothes.response.ClothUpdateResponseDto;
import com.stylemycloset.clothes.service.clothes.ClothService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/clothes")
@RequiredArgsConstructor
public class ClothesController {

  private final ClothService clothService;

  @PostMapping
  public ResponseEntity<ClothesDto> createCloth(
      @RequestPart("request") ClothesCreateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    ClothesDto response = clothService.createCloth(
        request,
        ClothBinaryContentRequest.from(image)
    );
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<ClothDtoCursorResponse> getClothes(
      @Valid @ModelAttribute ClothesSearchCondition clothesSearchCondition
  ) {
    ClothDtoCursorResponse response = clothService.getClothes(clothesSearchCondition);
    return ResponseEntity.ok(response);
  }

  @PatchMapping("/{clothesId}")
  public ResponseEntity<ClothUpdateResponseDto> updateCloth(
      @PathVariable Long clothesId,
      @RequestPart("request") ClothUpdateRequest request,
      @RequestPart(value = "image", required = false) MultipartFile image
  ) {
    ClothUpdateResponseDto response = clothService.updateCloth(
        clothesId,
        request,
        ClothBinaryContentRequest.from(image)
    );
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{clothesId}")
  public ResponseEntity<Void> deleteCloth(@PathVariable Long clothesId) {
    clothService.softDeleteCloth(clothesId);
    return ResponseEntity.ok().build();
  }

  @GetMapping("/extractions")
  public ResponseEntity<ClothesDto> extractClothInfo(
      @RequestParam("url") String url
  ) {
    ClothesDto extractedClothes = clothService.extractInfo(url);
    return ResponseEntity.ok(extractedClothes);
  }

}
