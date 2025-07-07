package com.example.demo.Controller;

import com.example.demo.SecurityConfigurations.JwtUtility;
import com.example.demo.entity.User;
import com.example.demo.service.UserService; // Import UserService
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${FRONTEND_URL:http://localhost:3000}")
    private String FRONT_END_ORIGIN="http://135.235.137.124:3000";

    @Autowired
    private UserService userService; // Use UserService

    @Autowired
    private JwtUtility jwtUtility;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        System.out.println("OAuth2 Authentication Success Handler triggered");
        
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        System.out.println("OAuth2 user attributes: " + oauthUser.getAttributes());
        
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        
        System.out.println("Email: " + email + ", Name: " + name);

        // Find user by email or create a new one using the consistent service method
        User user = userService.findByEmail(email);
        if (user == null) {
            System.out.println("User not found, creating new OAuth2 user...");
            user = userService.registerNewOAuth2User(name, email);
        }
        
        // Generate JWT
        String jwt = jwtUtility.generateToken(user);
        System.out.println("JWT Token generated for user: " + user.getEmail());

        // Redirect to frontend with token in URL
        String redirectUrl = FRONT_END_ORIGIN + "/oauth-success?token=" + jwt;
        System.out.println("Redirecting to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}