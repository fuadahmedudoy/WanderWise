package com.example.demo.Controller;

import com.example.demo.Repository.UserRepository;
import com.example.demo.SecurityConfigurations.JwtUtility;
import com.example.demo.entity.User;
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

    @Value("${frontend.origin:http://localhost:3000}")
    private String FRONT_END_ORIGIN;

    @Autowired
    private UserRepository userRepository;

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

        // Create user if doesn't exist
        User user = userRepository.findByEmail(email);
        if (user == null) {
            user = User.builder()
                    .email(email)
                    .username(name)
                    .role("USER")
                    .password("") // OAuth2 users don't need a password
                    .build();
            userRepository.save(user);
        }        // Generate JWT
        String jwt = jwtUtility.generateToken(user);
        System.out.println("JWT Token generated for user: " + user.getEmail());

        // Redirect to frontend with token in URL
        String redirectUrl = FRONT_END_ORIGIN + "/oauth-success?token=" + jwt;
        System.out.println("Redirecting to: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}