package com.example.demo.service;

import com.example.demo.Repository.BlogPostRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.CreateBlogPostRequest;
import com.example.demo.dto.BlogPostDTO;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminFileStorageService adminFileStorageService; 

    @Autowired
    private BlogInteractionService blogInteractionService;

   private BlogPostDTO convertToDTO(BlogPost blogPost) {
        return convertToDTO(blogPost, null);
    }

    private BlogPostDTO convertToDTO(BlogPost blogPost, UUID currentUserId) {
        BlogPostDTO dto = BlogPostDTO.builder()
                .id(blogPost.getId())
                .userId(blogPost.getUserId())
                .title(blogPost.getTitle())
                .content(blogPost.getContent())
                .imageUrl(blogPost.getImageUrl())
                .tags(blogPost.getTags())
                .isPublic(blogPost.isPublic())
                .createdAt(blogPost.getCreatedAt())
                .updatedAt(blogPost.getUpdatedAt())
                .username(blogPost.getUser() != null ? blogPost.getUser().getUsername() : null)
                .userEmail(blogPost.getUser() != null ? blogPost.getUser().getEmail() : null)
                .likeCount(blogInteractionService.getLikeCount(blogPost.getId()))
                .commentCount(blogInteractionService.getCommentCount(blogPost.getId()))
                .build();

        if (currentUserId != null) {
            dto.setIsLikedByCurrentUser(blogInteractionService.isLikedByUser(blogPost.getId(), currentUserId));
        }

        return dto;
    }

    @Transactional
    public BlogPostDTO createBlogPost(UUID userId, CreateBlogPostRequest request, MultipartFile imageFile) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = adminFileStorageService.saveDestinationImage(imageFile);
        }

        BlogPost blogPost = BlogPost.builder()
                .userId(userId)
                .user(user) 
                .title(request.getTitle())
                .content(request.getContent())
                .imageUrl(imageUrl)
                .tags(request.getTags() != null ? request.getTags() : new String[]{})
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        BlogPost savedBlogPost = blogPostRepository.save(blogPost);
        return convertToDTO(savedBlogPost);
    }

    public List<BlogPostDTO> getAllPublicBlogPosts() {
        return getAllPublicBlogPosts(null);
    }

    public List<BlogPostDTO> getAllPublicBlogPosts(UUID currentUserId) {
        List<BlogPost> posts = blogPostRepository.findByIsPublicTrueOrderByCreatedAtDesc();
        return posts.stream()
                .map(post -> convertToDTO(post, currentUserId))
                .collect(Collectors.toList());
    }

    public Optional<BlogPostDTO> getBlogPostById(UUID id) {
        return getBlogPostById(id, null);
    }

    public Optional<BlogPostDTO> getBlogPostById(UUID id, UUID currentUserId) {
        return blogPostRepository.findById(id)
                .map(post -> convertToDTO(post, currentUserId));
    }
    
    public List<BlogPostDTO> getUserBlogPosts(UUID userId) {
        List<BlogPost> posts = blogPostRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public BlogPostDTO updateBlogPost(UUID blogId, UUID userId, CreateBlogPostRequest request, MultipartFile imageFile) throws IOException {
        BlogPost existingPost = blogPostRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + blogId));

        if (!existingPost.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to update this blog post");
        }

        existingPost.setTitle(request.getTitle());
        existingPost.setContent(request.getContent());
        existingPost.setTags(request.getTags() != null ? request.getTags() : new String[]{});
        existingPost.setPublic(request.getIsPublic() != null ? request.getIsPublic() : true);

        if (imageFile != null && !imageFile.isEmpty()) {
            // Delete old image if exists
            if (existingPost.getImageUrl() != null && !existingPost.getImageUrl().isEmpty()) {
                adminFileStorageService.deleteDestinationImage(existingPost.getImageUrl());
            }
            existingPost.setImageUrl(adminFileStorageService.saveDestinationImage(imageFile));
        }

        existingPost.setUpdatedAt(LocalDateTime.now());
        BlogPost updatedPost = blogPostRepository.save(existingPost);
        return convertToDTO(updatedPost);
    }

    @Transactional
    public void deleteBlogPost(UUID blogId, UUID userId) {
        BlogPost existingPost = blogPostRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found with id: " + blogId));

        if (!existingPost.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to delete this blog post");
        }
        
        if (existingPost.getImageUrl() != null && !existingPost.getImageUrl().isEmpty()) {
            adminFileStorageService.deleteDestinationImage(existingPost.getImageUrl());
        }

        blogPostRepository.delete(existingPost);
    }
}