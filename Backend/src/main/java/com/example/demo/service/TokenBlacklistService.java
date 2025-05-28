package com.example.demo.service;

import com.example.demo.entity.BlacklistedToken;
import com.example.demo.Repository.BlacklistedTokenRepository;
import com.example.demo.SecurityConfigurations.JwtUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
@EnableScheduling
public class TokenBlacklistService {

    @Autowired
    private BlacklistedTokenRepository repository;

    @Autowired
    private JwtUtility jwtUtility;

    /**
     * Add a token to the blacklist
     */
    public void blacklistToken(String token) {
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setExpiryDate(jwtUtility.extractExpiration(token));
        repository.save(blacklistedToken);
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        return repository.existsById(token);
    }

    /**
     * Clean up expired tokens daily
     */
    @Scheduled(fixedRate = 86400000) // Daily cleanup
    @Transactional
    public void cleanupExpiredTokens() {
        repository.deleteByExpiryDateBefore(new Date());
    }
}
