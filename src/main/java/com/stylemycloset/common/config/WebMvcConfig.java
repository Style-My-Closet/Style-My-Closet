package com.stylemycloset.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.image.storage.path:storage/images}")
    private String baseStoragePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(baseStoragePath).toAbsolutePath().toString();
        registry.addResourceHandler("/files/images/**")
                .addResourceLocations("file:" + absolutePath + "/")
                .setCachePeriod(3600);
    }
}


