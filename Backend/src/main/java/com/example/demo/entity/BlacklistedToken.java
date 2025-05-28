 package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "blacklisted_tokens")
public class BlacklistedToken {
    @Id
    @Column(length = 500)
    private String token;

    private Date expiryDate;

    // Default constructor
    public BlacklistedToken() {
    }

    // Constructor with token
    public BlacklistedToken(String token, Date expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }

    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}
