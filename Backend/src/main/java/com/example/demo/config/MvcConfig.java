package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;
//comment
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // This configuration allows the Spring Boot application to serve uploaded files directly.
        // It is primarily used for local development when running the backend without the Nginx reverse proxy.
        // In the production Docker environment, Nginx is configured to serve these files directly for better performance.
        Path uploadPath = Paths.get(uploadDir);
        String resolvedPath = uploadPath.toUri().toString();

        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(resolvedPath);
    }
}