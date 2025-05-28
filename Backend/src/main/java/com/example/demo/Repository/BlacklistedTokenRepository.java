package com.example.demo.Repository;

import com.example.demo.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {
    void deleteByExpiryDateBefore(Date date);
}