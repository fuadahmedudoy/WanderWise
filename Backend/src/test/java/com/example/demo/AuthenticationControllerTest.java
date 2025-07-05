// package com.example.demo;

// import com.example.demo.Controller.AuthenticationController;
// import com.example.demo.SecurityConfigurations.JwtUtility;
// import com.example.demo.Repository.UserRepository;
// import com.example.demo.dto.LoginRequest;
// import com.example.demo.dto.RegisterRequest;
// import com.example.demo.dto.OtpVerificationRequest;
// import com.example.demo.entity.User;
// import com.example.demo.service.*;
// import com.example.demo.config.TestSecurityConfig;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
// @AutoConfigureMockMvc
// @WebMvcTest(AuthenticationController.class)
// @Import(TestSecurityConfig.class)
// public class AuthenticationControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private AuthenticationManager authenticationManager;

//     @MockBean
//     private JwtUtility jwtUtility;

//     @MockBean
//     private UserService userService;

//     @MockBean
//     private UserRepository userRepository;

//     @MockBean
//     private TokenBlacklistService tokenBlacklistService;

//     @MockBean
//     private OtpService otpService;

//     @MockBean
//     private PasswordEncoder passwordEncoder;

//     private User testUser;
//     private String testToken;

//     @BeforeEach
//     void setUp() {
//         testUser = new User();
//         testUser.setEmail("test@example.com");
//         testUser.setUsername("testuser");
//         testUser.setPassword("password123");
//         testUser.setRole("USER");

//         testToken = "test.jwt.token";
//     }

//     @Test
//     void loginSuccess() throws Exception {
//         LoginRequest loginRequest = new LoginRequest();
//         loginRequest.setEmail("test@example.com");
//         loginRequest.setPassword("password123");

//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenReturn(new UsernamePasswordAuthenticationToken(testUser, null));
//         when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
//         when(jwtUtility.generateToken(any(User.class))).thenReturn(testToken);

//         mockMvc.perform(post("/api/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.token").value(testToken))
//                 .andExpect(jsonPath("$.email").value(testUser.getEmail()))
//                 .andExpect(jsonPath("$.username").value(testUser.getUsername()))
//                 .andExpect(jsonPath("$.role").value(testUser.getRole()));
//     }

//     @Test
//     void loginFailure() throws Exception {
//         LoginRequest loginRequest = new LoginRequest();
//         loginRequest.setEmail("test@example.com");
//         loginRequest.setPassword("wrongpassword");

//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenThrow(new RuntimeException("Invalid credentials"));

//         mockMvc.perform(post("/api/login")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(loginRequest)))
//                 .andExpect(status().isUnauthorized())
//                 .andExpect(content().string("Invalid credentials"));
//     }

//     @Test
//     void signupSuccess() throws Exception {
//         RegisterRequest registerRequest = new RegisterRequest();
//         registerRequest.setEmail("new@example.com");
//         registerRequest.setUsername("newuser");
//         registerRequest.setPassword("password123");

//         doNothing().when(otpService).initiateRegistration(any(RegisterRequest.class));

//         mockMvc.perform(post("/api/signup")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(registerRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("OTP sent to your email. Please verify to complete registration."))
//                 .andExpect(jsonPath("$.email").value(registerRequest.getEmail()));
//     }

//     @Test
//     void verifyOtpSuccess() throws Exception {
//         OtpVerificationRequest otpRequest = new OtpVerificationRequest();
//         otpRequest.setEmail("test@example.com");
//         otpRequest.setOtp("123456");

//         when(otpService.verifyOtpAndCompleteRegistration(anyString(), anyString()))
//                 .thenReturn(testUser);

//         mockMvc.perform(post("/api/verify-otp")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content(objectMapper.writeValueAsString(otpRequest)))
//                 .andExpect(status().isCreated())
//                 .andExpect(jsonPath("$.message").value("Registration completed successfully"))
//                 .andExpect(jsonPath("$.username").value(testUser.getUsername()));
//     }

//     @Test
//     void logoutSuccess() throws Exception {
//         doNothing().when(tokenBlacklistService).blacklistToken(anyString());

//         mockMvc.perform(post("/api/logout")
//                 .header("Authorization", "Bearer " + testToken))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("Logged out successfully"));
//     }

//     @Test
//     void getCurrentUserSuccess() throws Exception {
//         when(jwtUtility.extractUserName(testToken)).thenReturn(testUser.getUsername());
//         when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
//         when(jwtUtility.validateToken(any(User.class), anyString())).thenReturn(true);
//         when(jwtUtility.generateToken(any(User.class))).thenReturn("new.test.token");

//         mockMvc.perform(get("/api/me")
//                 .header("Authorization", "Bearer " + testToken))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.username").value(testUser.getUsername()))
//                 .andExpect(jsonPath("$.email").value(testUser.getEmail()))
//                 .andExpect(jsonPath("$.role").value(testUser.getRole()));
//     }

//     @Test
//     void pingSuccess() throws Exception {
//         mockMvc.perform(get("/api/ping"))
//                 .andExpect(status().isOk())
//                 .andExpect(content().string("API is up and running"));
//     }
// }