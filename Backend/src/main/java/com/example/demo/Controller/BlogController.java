package com.example.demo.Controller;

import com.example.demo.dto.CreateBlogPostRequest;
import com.example.demo.dto.BlogPostDTO;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.User;
import com.example.demo.service.BlogPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private ObjectMapper objectMapper; // For parsing JSON part of multipart request

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createBlogPost(
            @AuthenticationPrincipal User user,
            @RequestPart("blogPost") @Valid String blogPostJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required to create a blog post."));
        }

        try {
            CreateBlogPostRequest request = objectMapper.readValue(blogPostJson, CreateBlogPostRequest.class);
            BlogPostDTO newBlogPost = blogPostService.createBlogPost(user.getId(), request, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(newBlogPost);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error processing blog post or image: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<BlogPostDTO>> getAllPublicBlogPosts() {
        List<BlogPostDTO> blogPosts = blogPostService.getAllPublicBlogPosts();
        return ResponseEntity.ok(blogPosts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBlogPostById(@PathVariable UUID id) {
        return blogPostService.getBlogPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/my-blogs")
    public ResponseEntity<?> getMyBlogPosts(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required to view your blog posts."));
        }
        List<BlogPostDTO> userBlogs = blogPostService.getUserBlogPosts(user.getId());
        return ResponseEntity.ok(userBlogs);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateBlogPost(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user,
            @RequestPart("blogPost") @Valid String blogPostJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required to update a blog post."));
        }
        
        try {
            CreateBlogPostRequest request = objectMapper.readValue(blogPostJson, CreateBlogPostRequest.class);
            BlogPostDTO updatedBlogPost = blogPostService.updateBlogPost(id, user.getId(), request, image);
            return ResponseEntity.ok(updatedBlogPost);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error processing blog post or image: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBlogPost(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Authentication required to delete a blog post."));
        }
        
        try {
            blogPostService.deleteBlogPost(id, user.getId());
            return ResponseEntity.ok(Map.of("message", "Blog post deleted successfully."));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}