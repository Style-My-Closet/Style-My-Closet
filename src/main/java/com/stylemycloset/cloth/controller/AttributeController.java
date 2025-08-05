package com.stylemycloset.cloth.controller;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.response.AttributeListResponseDto;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.service.ClothAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attributes")
@RequiredArgsConstructor
public class AttributeController {

    private final ClothAttributeService clothAttributeService;

    @GetMapping
    public ResponseEntity<AttributeListResponseDto> getAttributes(@Valid @ModelAttribute CursorDto cursorDto) {
        AttributeListResponseDto response = clothAttributeService.findAttributes(cursorDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AttributeResponseDto> createAttribute(
            @Valid @RequestBody ClothesAttributeDefCreateRequest request) {
        
        AttributeResponseDto response = clothAttributeService.createAttribute(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{attributeId}")
    public ResponseEntity<AttributeResponseDto> updateAttribute(
            @PathVariable Long attributeId,
            @Valid @RequestBody ClothesAttributeDefCreateRequest request) {
        
        AttributeResponseDto response = clothAttributeService.updateAttribute(attributeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{attributeId}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable Long attributeId) {
        
        clothAttributeService.deleteAttributeById(attributeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{attributeId}/options")
    public ResponseEntity<AttributeResponseDto> addAttributeOptions(
            @PathVariable Long attributeId,
            @RequestBody List<String> optionValues) {
        
        AttributeResponseDto response = clothAttributeService.addAttributeOptions(attributeId, optionValues);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{attributeId}/options")
    public ResponseEntity<AttributeResponseDto> removeAttributeOptions(
            @PathVariable Long attributeId,
            @RequestBody List<String> optionValues) {
        
        AttributeResponseDto response = clothAttributeService.removeAttributeOptions(attributeId, optionValues);
        return ResponseEntity.ok(response);
    }
} 