
package com.example.demo.Repository;

import com.example.demo.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, UUID> {
    List<BlogPost> findByIsPublicTrueOrderByCreatedAtDesc();
    List<BlogPost> findByUserIdOrderByCreatedAtDesc(UUID userId);
}