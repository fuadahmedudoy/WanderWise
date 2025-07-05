// package com.example.demo.Controller;

// import com.example.demo.Repository.UserRepository;
// import com.example.demo.SecurityConfigurations.JwtUtility;
// import com.example.demo.dto.LoginRequest;
// import com.example.demo.dto.OtpVerificationRequest;
// import com.example.demo.dto.RegisterRequest;
// import com.example.demo.entity.User;
// import com.example.demo.service.OtpService;
// import com.example.demo.service.TokenBlacklistService;
// import com.example.demo.service.UserService;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;
// import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(AuthenticationController.class)
// class AuthenticationControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Mock
//     private PasswordEncoder passwordEncoder;

//     @Mock
//     private JwtUtility jwtUtility;

//     @Mock
//     private UserService userService;

//     @Mock
//     private UserRepository userRepository;

//     @Mock
//     private AuthenticationManager authenticationManager;

//     @Mock
//     private TokenBlacklistService tokenBlacklistService;

//     @Mock
//     private OtpService otpService;

//     @Autowired
//     private ObjectMapper objectMapper;

//     private User testUser;
//     private LoginRequest loginRequest;
//     private RegisterRequest registerRequest;
//     private OtpVerificationRequest otpRequest;

//     @BeforeEach
//     void setUp() {
//         testUser = new User();
//         testUser.setUsername("testuser");
//         testUser.setEmail("test@example.com");
//         testUser.setRole("USER");

//         loginRequest = new LoginRequest();
//         loginRequest.setEmail("test@example.com");
//         loginRequest.setPassword("password123");

//         registerRequest = new RegisterRequest();
//         registerRequest.setUsername("testuser");
//         registerRequest.setEmail("test@example.com");
//         registerRequest.setPassword("password123");

//         otpRequest = new OtpVerificationRequest();
//         otpRequest.setEmail("test@example.com");
//         otpRequest.setOtp("123456");
//     }

//     @Test
//     @WithMockUser
//     void login_Success() throws Exception {
//         // Given
//         Authentication auth = mock(Authentication.class);
//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
//         when(userRepository.findByEmail(anyString())).thenReturn(Optional.ofNullable(testUser));
//         when(jwtUtility.generateToken(any(User.class))).thenReturn("jwt-token");

//         // When & Then
//         mockMvc.perform(post("/api/login")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").value("jwt-token"))
//                 .andExpect(jsonPath("$.username").value("testuser"))
//                 .andExpect(jsonPath("$.email").value("test@example.com"))
//                 .andExpect(jsonPath("$.role").value("USER"));

//         verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
//         verify(userRepository).findByEmail("test@example.com");
//         verify(jwtUtility).generateToken(testUser);
//     }

//     @Test
//     @WithMockUser
//     void login_InvalidCredentials() throws Exception {
//         // Given
//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenThrow(new RuntimeException("Invalid credentials"));

//         // When & Then
//         mockMvc.perform(post("/api/login")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(content().string("Invalid credentials"));

// //                .andExpected(status().isUnauthorized())
// //                .andExpect(content().string("Invalid credentials"));
//     }

//     @Test
//     @WithMockUser
//     void signup_Success() throws Exception {
//         // Given
//         doNothing().when(otpService).initiateRegistration(any(RegisterRequest.class));

//         // When & Then
//         mockMvc.perform(post("/api/signup")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(registerRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("OTP sent to your email. Please verify to complete registration."))
//                 .andExpect(jsonPath("$.email").value("test@example.com"));

//         verify(otpService).initiateRegistration(any(RegisterRequest.class));
//     }

//     @Test
//     @WithMockUser
//     void signup_UserAlreadyExists() throws Exception {
//         // Given
//         doThrow(new RuntimeException("Error: Username is already taken!"))
//                 .when(otpService).initiateRegistration(any(RegisterRequest.class));

//         // When & Then
//         mockMvc.perform(post("/api/signup")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(registerRequest)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
//     }

//     @Test
//     @WithMockUser
//     void verifyOtp_Success() throws Exception {
//         // Given
//         when(otpService.verifyOtpAndCompleteRegistration(anyString(), anyString())).thenReturn(testUser);

//         // When & Then
//         mockMvc.perform(post("/api/verify-otp")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(otpRequest)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.message").value("Registration completed successfully"))
//                 .andExpect(jsonPath("$.username").value("testuser"));

//         verify(otpService).verifyOtpAndCompleteRegistration("test@example.com", "123456");
//     }

//     @Test
//     @WithMockUser
//     void verifyOtp_InvalidOtp() throws Exception {
//         // Given
//         doThrow(new RuntimeException("Invalid OTP"))
//                 .when(otpService).verifyOtpAndCompleteRegistration(anyString(), anyString());

//         // When & Then
//         mockMvc.perform(post("/api/verify-otp")
//                 .with(csrf())
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(otpRequest)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Invalid OTP"));
//     }

//     @Test
//     @WithMockUser
//     void logout_Success() throws Exception {
//         // Given
//         doNothing().when(tokenBlacklistService).blacklistToken(anyString());

//         // When & Then
//         mockMvc.perform(post("/api/logout")
//                 .with(csrf())
//                 .header("Authorization", "Bearer jwt-token"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Logged out successfully"));

//         verify(tokenBlacklistService).blacklistToken("jwt-token");
//     }
// }