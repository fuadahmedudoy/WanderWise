// package com.example.demo.User;

// import com.example.demo.Repository.UserRepository;
// import com.example.demo.entity.User;
// import com.example.demo.entity.UserProfile;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.context.annotation.Import;
// import org.springframework.test.context.TestPropertySource;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.Optional;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest
// @TestPropertySource(locations = "classpath:application-test.properties")
// @Transactional
// class UserRepositoryTest {

//     @Autowired
//     private UserRepository userRepository;

//     private User testUser;

//     @BeforeEach
//     void setUp() {
//         // Clean up any existing data
//         userRepository.deleteAll();
        
//         testUser = new User();
//         testUser.setUsername("testuser");
//         testUser.setEmail("test@example.com");
//         testUser.setPassword("encodedPassword");
//         testUser.setRole("USER");

//         UserProfile userProfile = new UserProfile();
//         userProfile.setUser(testUser);
//         testUser.setUserProfile(userProfile);
//     }

//     @Test
//     void testFindByEmail_Success() {
//         // Arrange
//         User savedUser = userRepository.save(testUser);

//         // Act
//         Optional<User> foundUser = userRepository.findByEmail("test@example.com");

//         // Assert
//         assertTrue(foundUser.isPresent());
//         assertEquals("testuser", foundUser.get().getUsername());
//         assertEquals("test@example.com", foundUser.get().getEmail());
//         assertEquals("USER", foundUser.get().getRole());
//     }

//     @Test
//     void testFindByEmail_NotFound() {
//         // Act
//         Optional<User> foundUser = userRepository.findByEmail("notfound@example.com");

//         // Assert
//         assertFalse(foundUser.isPresent());
//     }

//     @Test
//     void testFindByUsername_Success() {
//         // Arrange
//         User savedUser = userRepository.save(testUser);

//         // Act
//         Optional<User> foundUser = userRepository.findByUsername("testuser");

//         // Assert
//         assertTrue(foundUser.isPresent());
//         assertEquals("testuser", foundUser.get().getUsername());
//         assertEquals("test@example.com", foundUser.get().getEmail());
//     }

//     @Test
//     void testFindByUsername_NotFound() {
//         // Act
//         Optional<User> foundUser = userRepository.findByUsername("notfound");

//         // Assert
//         assertFalse(foundUser.isPresent());
//     }

//     @Test
//     void testSaveUser_Success() {
//         // Act
//         User savedUser = userRepository.save(testUser);

//         // Assert
//         assertNotNull(savedUser.getId());
//         assertEquals("testuser", savedUser.getUsername());
//         assertEquals("test@example.com", savedUser.getEmail());
//         assertNotNull(savedUser.getUserProfile());
//     }

//     @Test
//     void testDeleteUser_Success() {
//         // Arrange
//         User savedUser = userRepository.save(testUser);
//         UUID userId = savedUser.getId();

//         // Act
//         userRepository.delete(savedUser);

//         // Assert
//         Optional<User> deletedUser = userRepository.findById(userId);
//         assertFalse(deletedUser.isPresent());
//     }
// }