package com.example.demo;

import com.example.demo.service.FileStorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WanderWiseApp implements CommandLineRunner { 

    @Resource
    FileStorageService storageService;

    public static void main(String[] args) {
        SpringApplication.run(WanderWiseApp.class, args);
    }

    @Override
    public void run(String... arg) throws Exception {
        // FileStorageService initialization is handled in the constructor
        // No need to call init() method
    }
}