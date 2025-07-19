package com.example.demo.Repository;

import com.example.demo.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, UUID> {
    
    Optional<BlogLike> findByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    
    @Query("SELECT COUNT(bl) FROM BlogLike bl WHERE bl.blogPostId = :blogPostId")
    Long countByBlogPostId(@Param("blogPostId") UUID blogPostId);
    
    List<BlogLike> findByBlogPostId(UUID blogPostId);
    
    void deleteByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
    
    boolean existsByBlogPostIdAndUserId(UUID blogPostId, UUID userId);
}