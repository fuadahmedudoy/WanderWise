package com.example.demo.Controller;

import com.example.demo.dto.BlogCommentDTO;
import com.example.demo.dto.BlogLikeDTO;
import com.example.demo.dto.CreateCommentRequest;
import com.example.demo.entity.User;
import com.example.demo.service.BlogInteractionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/blogs/{blogId}/interactions")
public class BlogInteractionController {

    @Autowired
    private BlogInteractionService blogInteractionService;

    // Like endpoints
    @PostMapping("/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to like posts"));
        }

        try {
            boolean isLiked = blogInteractionService.toggleLike(blogId, user.getId());
            Long likeCount = blogInteractionService.getLikeCount(blogId);
            
            return ResponseEntity.ok(Map.of(
                    "isLiked", isLiked,
                    "likeCount", likeCount,
                    "message", isLiked ? "Post liked successfully" : "Post unliked successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/likes")
    public ResponseEntity<?> getBlogLikes(@PathVariable UUID blogId) {
        try {
            List<BlogLikeDTO> likes = blogInteractionService.getBlogLikes(blogId);
            Long likeCount = blogInteractionService.getLikeCount(blogId);
            
            return ResponseEntity.ok(Map.of(
                    "likes", likes,
                    "likeCount", likeCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Comment endpoints
    @PostMapping("/comments")
    public ResponseEntity<?> addComment(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateCommentRequest request) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to comment"));
        }

        try {
            BlogCommentDTO comment = blogInteractionService.addComment(blogId, user.getId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getBlogComments(@PathVariable UUID blogId) {
        try {
            List<BlogCommentDTO> comments = blogInteractionService.getBlogComments(blogId);
            Long commentCount = blogInteractionService.getCommentCount(blogId);
            
            return ResponseEntity.ok(Map.of(
                    "comments", comments,
                    "commentCount", commentCount
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable UUID blogId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User user,
            @RequestBody Map<String, String> request) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to update comments"));
        }

        try {
            String newContent = request.get("content");
            if (newContent == null || newContent.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Comment content cannot be empty"));
            }

            BlogCommentDTO updatedComment = blogInteractionService.updateComment(commentId, user.getId(), newContent);
            return ResponseEntity.ok(updatedComment);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable UUID blogId,
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required to delete comments"));
        }

        try {
            blogInteractionService.deleteComment(commentId, user.getId());
            return ResponseEntity.ok(Map.of("message", "Comment deleted successfully"));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}