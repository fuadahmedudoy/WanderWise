package com.example.demo;

import com.example.demo.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthenticationControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testLoginSuccess() {
        // Create a valid login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@email.com");
        loginRequest.setPassword("test1234");

        // Send the login request
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/login", request, String.class);

        // Assert the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

    }

    @Test
    void testLoginFailure() {
        // Create an invalid login request
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("invaliduser@example.com");
        loginRequest.setPassword("wrongpassword");

        // Send the login request
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest);
        ResponseEntity<String> response = restTemplate.postForEntity("/api/login", request, String.class);

        // Assert the response
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Invalid credentials"));
    }
}