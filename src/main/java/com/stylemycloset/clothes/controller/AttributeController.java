package com.stylemycloset.clothes.controller;

import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeDefinitionCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeDefinitionUpdateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/clothes/attribute-defs")
@RequiredArgsConstructor
public class AttributeController {

  private final ClothAttributeService clothAttributeService;

  @GetMapping
  public ResponseEntity<ClothesAttributeDefinitionDtoCursorResponse> getAttributes(
      @Valid @ModelAttribute ClothesAttributeSearchCondition attributeSearchCondition
  ) {
    ClothesAttributeDefinitionDtoCursorResponse response = clothAttributeService.getAttributes(
        attributeSearchCondition
    );
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ClothesAttributeDefinitionDto> createAttribute(
      @Valid @RequestBody ClothesAttributeDefinitionCreateRequest request
  ) {
    ClothesAttributeDefinitionDto attribute = clothAttributeService.createAttribute(request);
    return ResponseEntity.ok(attribute);
  }

  @PatchMapping(value = "/{definitionId}")
  public ResponseEntity<ClothesAttributeDefinitionDto> updateAttribute(
      @PathVariable("definitionId") Long definitionId,
      @Valid @RequestBody ClothesAttributeDefinitionUpdateRequest request
  ) {
    ClothesAttributeDefinitionDto attribute = clothAttributeService.updateAttribute(
        definitionId,
        request
    );
    return ResponseEntity.ok(attribute);
  }

  @DeleteMapping("/{definitionId}")
  public ResponseEntity<Void> deleteAttribute(
      @PathVariable("definitionId") Long definitionId
  ) {
    clothAttributeService.deleteAttributeById(definitionId);
    return ResponseEntity.ok().build();
  }

} 