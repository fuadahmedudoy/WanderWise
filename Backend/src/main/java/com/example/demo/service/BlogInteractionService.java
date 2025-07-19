package com.example.demo.service;

import com.example.demo.Repository.BlogCommentRepository;
import com.example.demo.Repository.BlogLikeRepository;
import com.example.demo.Repository.BlogPostRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.dto.BlogCommentDTO;
import com.example.demo.dto.BlogLikeDTO;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.entity.BlogComment;
import com.example.demo.entity.BlogLike;
import com.example.demo.entity.BlogPost;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BlogInteractionService {

    @Autowired
    private BlogLikeRepository blogLikeRepository;

    @Autowired
    private BlogCommentRepository blogCommentRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private UserRepository userRepository;

    // Like functionality
    @Transactional
    public boolean toggleLike(UUID blogPostId, UUID userId) {
        // Verify blog post exists
        blogPostRepository.findById(blogPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found"));

        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<BlogLike> existingLike = blogLikeRepository.findByBlogPostIdAndUserId(blogPostId, userId);

        if (existingLike.isPresent()) {
            // Unlike
            blogLikeRepository.delete(existingLike.get());
            return false;
        } else {
            // Like
            BlogLike newLike = BlogLike.builder()
                    .blogPostId(blogPostId)
                    .userId(userId)
                    .build();
            blogLikeRepository.save(newLike);
            return true;
        }
    }

    public Long getLikeCount(UUID blogPostId) {
        return blogLikeRepository.countByBlogPostId(blogPostId);
    }

    public boolean isLikedByUser(UUID blogPostId, UUID userId) {
        return blogLikeRepository.existsByBlogPostIdAndUserId(blogPostId, userId);
    }

    public List<BlogLikeDTO> getBlogLikes(UUID blogPostId) {
        List<BlogLike> likes = blogLikeRepository.findByBlogPostId(blogPostId);
        return likes.stream()
                .map(this::convertLikeToDTO)
                .collect(Collectors.toList());
    }

    // Comment functionality
    @Transactional
    public BlogCommentDTO addComment(UUID blogPostId, UUID userId, CreateCommentRequest request) {
        // Verify blog post exists
        BlogPost blogPost = blogPostRepository.findById(blogPostId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found"));

        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // If it's a reply, verify parent comment exists
        if (request.getParentCommentId() != null) {
            blogCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }

        BlogComment comment = BlogComment.builder()
                .blogPostId(blogPostId)
                .userId(userId)
                .content(request.getContent())
                .parentCommentId(request.getParentCommentId())
                .build();

        BlogComment savedComment = blogCommentRepository.save(comment);
        return convertCommentToDTO(savedComment, user);
    }

    public List<BlogCommentDTO> getBlogComments(UUID blogPostId) {
        List<BlogComment> comments = blogCommentRepository
                .findByBlogPostIdAndParentCommentIdIsNullOrderByCreatedAtDesc(blogPostId);
        
        return comments.stream()
                .map(comment -> {
                    User user = userRepository.findById(comment.getUserId()).orElse(null);
                    BlogCommentDTO dto = convertCommentToDTO(comment, user);
                    
                    // Load replies
                    List<BlogComment> replies = blogCommentRepository
                            .findByParentCommentIdOrderByCreatedAtAsc(comment.getId());
                    List<BlogCommentDTO> replyDTOs = replies.stream()
                            .map(reply -> {
                                User replyUser = userRepository.findById(reply.getUserId()).orElse(null);
                                return convertCommentToDTO(reply, replyUser);
                            })
                            .collect(Collectors.toList());
                    dto.setReplies(replyDTOs);
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Long getCommentCount(UUID blogPostId) {
        return blogCommentRepository.countByBlogPostId(blogPostId);
    }

    @Transactional
    public BlogCommentDTO updateComment(UUID commentId, UUID userId, String newContent) {
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to update this comment");
        }

        comment.setContent(newContent);
        BlogComment updatedComment = blogCommentRepository.save(comment);
        
        User user = userRepository.findById(userId).orElse(null);
        return convertCommentToDTO(updatedComment, user);
    }

    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUserId().equals(userId)) {
            throw new SecurityException("User not authorized to delete this comment");
        }

        blogCommentRepository.delete(comment);
    }

    // Helper methods
    private BlogLikeDTO convertLikeToDTO(BlogLike like) {
        User user = userRepository.findById(like.getUserId()).orElse(null);
        return BlogLikeDTO.builder()
                .id(like.getId())
                .blogPostId(like.getBlogPostId())
                .userId(like.getUserId())
                .username(user != null ? user.getUsername() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .createdAt(like.getCreatedAt())
                .build();
    }

    private BlogCommentDTO convertCommentToDTO(BlogComment comment, User user) {
        return BlogCommentDTO.builder()
                .id(comment.getId())
                .blogPostId(comment.getBlogPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .username(user != null ? user.getUsername() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}