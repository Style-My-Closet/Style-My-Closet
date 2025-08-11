package com.stylemycloset.cloth.service;

import com.stylemycloset.cloth.dto.ClothesAttributeDefCreateRequest;
import com.stylemycloset.cloth.dto.response.AttributeResponseDto;
import com.stylemycloset.cloth.entity.ClothingAttribute;
import com.stylemycloset.cloth.mapper.AttributeResponseMapper;
import com.stylemycloset.cloth.mapper.ClothesAttributeDefMapper;
import com.stylemycloset.cloth.repository.ClothingAttributeRepository;
import com.stylemycloset.cloth.repository.ClothingAttributeValueRepository;
import com.stylemycloset.notification.event.domain.ClothAttributeChangedEvent;
import com.stylemycloset.notification.event.domain.NewClothAttributeEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClothAttributeServiceEventTest {

    @Mock private ClothingAttributeRepository clothingAttributeRepository;
    @Mock private ClothingAttributeValueRepository clothingAttributeValueRepository;
    @Mock private AttributeCacheService attributeCacheService;
    @Mock private ClothListCacheService clothListCacheService;
    @Mock private AttributeResponseMapper attributeResponseMapper;
    @Mock private ClothesAttributeDefMapper clothesAttributeDefMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks private ClothAttributeService clothAttributeService;

    @Test
    void createAttribute_publishes_NewClothAttributeEvent() {
        // given
        ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("Color", List.of("Black"));
        when(clothingAttributeRepository.findByName("Color")).thenReturn(Optional.empty());
        ClothingAttribute saved = ClothingAttribute.createWithOptions("Color", List.of("Black"));
        when(clothingAttributeRepository.save(any(ClothingAttribute.class))).thenReturn(saved);
        when(attributeResponseMapper.toDto(any(ClothingAttribute.class))).thenReturn(new AttributeResponseDto( null, "Color", List.of() ));

        // when
        clothAttributeService.createAttribute(request);

        // then
        verify(eventPublisher).publishEvent(argThat(isNewEventWithName("Color")));
        verify(attributeCacheService).evictAttributeCount();
        verify(clothListCacheService).evictAttributeListFirstPage();
    }

    @Test
    void updateAttribute_publishes_ClothAttributeChangedEvent() {
        // given
        Long id = 10L;
        ClothingAttribute existing = ClothingAttribute.createWithOptions("Material", List.of("Cotton"));
        when(clothingAttributeRepository.findById(anyLong())).thenReturn(Optional.of(existing));
        when(clothingAttributeRepository.save(any(ClothingAttribute.class))).thenAnswer(inv -> inv.getArgument(0));
        when(attributeResponseMapper.toDto(any(ClothingAttribute.class))).thenReturn(new AttributeResponseDto( null, "Fabric", List.of() ));
        ClothesAttributeDefCreateRequest request = new ClothesAttributeDefCreateRequest("Fabric", List.of("Cotton", "Linen"));

        // when
        clothAttributeService.updateAttribute(id, request);

        // then
        verify(eventPublisher).publishEvent(argThat(isChangedEventWithName("Fabric")));
        verify(clothListCacheService).evictAttributeListFirstPage();
    }

    private ArgumentMatcher<Object> isNewEventWithName(String expectedName) {
        return evt -> (evt instanceof NewClothAttributeEvent e) && expectedName.equals(e.attributeName());
    }

    private ArgumentMatcher<Object> isChangedEventWithName(String expectedName) {
        return evt -> (evt instanceof ClothAttributeChangedEvent e) && expectedName.equals(e.changedAttributeName());
    }
}
