package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This platform-independent way to configure the resource handler.
        // It correctly creates a 'file:' URI for both Windows and Linux environments.
        Path uploadPath = Paths.get(uploadDir);
        String resolvedPath = uploadPath.toUri().toString();

        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(resolvedPath);
    }
}