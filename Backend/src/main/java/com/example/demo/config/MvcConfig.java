package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resolvedPath = Paths.get(uploadDir).toAbsolutePath().normalize().toString();
        // On Windows, the path might start with a drive letter like C:\. 
        // We need to format it for the resource handler.
        if (resolvedPath.matches("^[a-zA-Z]:\\\\.*")) {
            resolvedPath = "file:///" + resolvedPath.replace("\\", "/") + "/";
        } else {
            resolvedPath = "file:" + resolvedPath + "/";
        }
        
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations(resolvedPath);
    }
}