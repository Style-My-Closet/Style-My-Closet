package com.stylemycloset.clothes.controller;

import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDto;
import com.stylemycloset.clothes.dto.attribute.ClothesAttributeDefinitionDtoCursorResponse;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeCreateRequest;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeSearchCondition;
import com.stylemycloset.clothes.dto.attribute.request.ClothesAttributeUpdateRequest;
import com.stylemycloset.clothes.service.attribute.ClothAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clothes/attribute-defs")
@RequiredArgsConstructor
public class AttributeController {

  private final ClothAttributeService clothAttributeService;

  @PostMapping
  public ResponseEntity<ClothesAttributeDefinitionDto> createAttribute(
      @Valid @RequestBody ClothesAttributeCreateRequest request
  ) {
    ClothesAttributeDefinitionDto attribute = clothAttributeService.createAttribute(request);
    return ResponseEntity.ok(attribute);
  }

  @GetMapping
  public ResponseEntity<ClothesAttributeDefinitionDtoCursorResponse> getAttributes(
      @Valid @ModelAttribute ClothesAttributeSearchCondition attributeSearchCondition
  ) {
    ClothesAttributeDefinitionDtoCursorResponse response = clothAttributeService.getAttributes(
        attributeSearchCondition
    );
    return ResponseEntity.ok(response);
  }

  @PatchMapping(value = "/{definitionId}")
  public ResponseEntity<ClothesAttributeDefinitionDto> updateAttribute(
      @PathVariable("definitionId") Long definitionId,
      @Valid @RequestBody ClothesAttributeUpdateRequest request
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