package com.example.demo;

import com.example.demo.SecurityConfigurations.SecurityConfiguration;
import com.example.demo.SecurityConfigurations.JwtAuthFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private SecurityConfiguration securityConfiguration;

	@Test
	void contextLoads() {
	}

	@Test
	void testCorsConfigurationSource() {
		CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();
		CorsConfiguration corsConfig = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfig);
		assertTrue(corsConfig.getAllowedOrigins().contains("http://localhost:3000"));
		assertTrue(corsConfig.getAllowedMethods().containsAll(
				List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")));
		assertTrue(corsConfig.getAllowedHeaders().containsAll(
				List.of("Authorization", "Content-Type", "X-Requested-With")));
		assertTrue(corsConfig.getAllowCredentials());
	}

	@Test
	void testAllowedOriginsFromEnv() {
		System.setProperty("frontend.origin", "http://example.com");
		CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();
		CorsConfiguration corsConfig = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfig);
		assertFalse(corsConfig.getAllowedOrigins().contains("http://example.com"));
	}

	@Test
	void testAllowedMethods() {
		CorsConfigurationSource source = securityConfiguration.corsConfigurationSource();
		CorsConfiguration corsConfig = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfig);
		assertTrue(corsConfig.getAllowedMethods().containsAll(
				List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")));
	}

	@Test
	void testJwtAuthFilterBean() {
		JwtAuthFilter jwtAuthFilter = securityConfiguration.jwtAuthFilter();
		assertNotNull(jwtAuthFilter);
	}

	@Test
	void testAuthenticationProvider() {
		PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		AuthenticationProvider provider = securityConfiguration.getAuthenticationProvider();

		assertNotNull(provider);
		assertTrue(provider instanceof AuthenticationProvider);
	}

	@Test
	void testSecurityFilterChain() throws Exception {
		AuthenticationProvider authenticationProvider = mock(AuthenticationProvider.class);

		SecurityFilterChain filterChain = securityConfiguration.getSecurityFilterChain(mock(org.springframework.security.config.annotation.web.builders.HttpSecurity.class));
		assertNotNull(filterChain);
	}
}