// package com.example.demo;

// import com.example.demo.SecurityConfigurations.SecurityConfiguration;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.security.authentication.AuthenticationProvider;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Missing import added

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.mock;

// @SpringBootTest
// class DemoApplicationTests {

//     @Autowired
//     private SecurityConfiguration securityConfiguration;

//     @Test
//     void contextLoads() {
//         // Ensure the application context loads successfully
//     }

//     @Test
//     void testAuthenticationProvider() {
//         // Test the authentication provider
//         PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//         AuthenticationProvider provider = securityConfiguration.authenticationProvider(passwordEncoder);

//         assertNotNull(provider);
//         assertTrue(provider instanceof AuthenticationProvider);
//     }

//     @Test
//     void testSecurityFilterChain() throws Exception {
//         // Test the security filter chain
//         HttpSecurity httpSecurity = mock(HttpSecurity.class);
//         AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);

//         SecurityFilterChain filterChain = securityConfiguration.getSecurityFilterChain(httpSecurity, authenticationProvider);
//         assertNotNull(filterChain);
//     }
// }