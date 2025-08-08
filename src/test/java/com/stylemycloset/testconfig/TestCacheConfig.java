package com.stylemycloset.testconfig;

import com.stylemycloset.cloth.service.ClothListCacheService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.function.Supplier;

@TestConfiguration
public class TestCacheConfig {

    @Bean
    @Primary
    public ClothListCacheService clothListCacheServiceMock() {
        ClothListCacheService mock = Mockito.mock(ClothListCacheService.class, Mockito.withSettings().lenient());

        Mockito.when(mock.isFirstPage(Mockito.any())).thenReturn(false);
        Mockito.when(mock.isNoKeywordSearch(Mockito.any())).thenReturn(true);

        Mockito.when(mock.getAttributeListFirstPage(Mockito.any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });

        Mockito.when(mock.getClothListFirstPage(Mockito.any(), Mockito.any()))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(1);
                    return supplier.get();
                });

        return mock;
    }
}


