// package com.example.demo.User;

// import com.example.demo.Repository.UserRepository;
// import com.example.demo.entity.User;
// import com.example.demo.entity.UserProfile;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.domain.EntityScan;
// import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
// import org.springframework.test.context.TestPropertySource;

// import java.util.Optional;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.*;

// @DataJpaTest
// @TestPropertySource(locations = "classpath:application-test.properties")
// @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
// @EnableJpaRepositories(basePackages = "com.example.demo.Repository")
// @EntityScan(basePackages = "com.example.demo.entity")
// class UserRepositoryTest {

//     @Autowired
//     private TestEntityManager entityManager;

//     @Autowired
//     private UserRepository userRepository;

//     private User testUser;

//     @BeforeEach
//     void setUp() {
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
//         entityManager.persistAndFlush(testUser);

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
//         entityManager.persistAndFlush(testUser);

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
//         User savedUser = entityManager.persistAndFlush(testUser);
//         UUID userId = savedUser.getId();

//         // Act
//         userRepository.delete(savedUser);
//         entityManager.flush();

//         // Assert
//         User deletedUser = userRepository.findById(userId).orElse(null);
//         assertNull(deletedUser);
//     }
// }