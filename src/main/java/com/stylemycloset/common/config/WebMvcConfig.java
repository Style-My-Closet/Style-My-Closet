package com.stylemycloset.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Local file serving removed after S3 migration

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {}
}


