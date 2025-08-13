package com.stylemycloset.cloth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.CursorDto;
import com.stylemycloset.cloth.dto.ClothesAttributeDefDto;
import com.stylemycloset.cloth.dto.response.PaginatedResponse;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.service.ClothAttributeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clothes/attribute-defs")
@RequiredArgsConstructor
public class AttributeController {

    private final ClothAttributeService clothAttributeService;
    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<PaginatedResponse<ClothesAttributeDefDto>> getAttributes(@Valid @ModelAttribute CursorDto cursorDto) {
        PaginatedResponse<ClothesAttributeDefDto> response = clothAttributeService.findAttributes(cursorDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<AttributeResponseDto> createAttribute(
            @Valid @RequestBody ClothesAttributeDefCreateRequest request) {

        AttributeResponseDto response = clothAttributeService.createAttribute(request);
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/{attributeId}", method = {RequestMethod.PATCH, RequestMethod.PUT}, consumes = "application/json")
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

} 