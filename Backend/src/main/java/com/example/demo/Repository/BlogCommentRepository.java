package com.example.demo.Repository;

import com.example.demo.entity.BlogComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, UUID> {
    
    List<BlogComment> findByBlogPostIdAndParentCommentIdIsNullOrderByCreatedAtDesc(UUID blogPostId);
    
    List<BlogComment> findByParentCommentIdOrderByCreatedAtAsc(UUID parentCommentId);
    
    @Query("SELECT COUNT(bc) FROM BlogComment bc WHERE bc.blogPostId = :blogPostId")
    Long countByBlogPostId(@Param("blogPostId") UUID blogPostId);
    
    List<BlogComment> findByBlogPostIdOrderByCreatedAtDesc(UUID blogPostId);
}