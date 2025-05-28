package com.example.demo.SecurityConfigurations;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtility {
    @Value("${jwt.secret}")
    private String SECRET_KEY ;

    @Value("${jwt.expiration}")
    private long expirationMs;

    public SecretKey getSecretKey(){
        byte[] bytes = Base64.getDecoder().decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expirationMs))
                .signWith(getSecretKey() , SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public boolean isExpired( String token){
        Date exp = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return exp.before(new Date());
    }
    public boolean validateToken(UserDetails userDetails , String token){
        return userDetails.getUsername().equals( extractUserName(token) ) && !isExpired(token);
    }

    // Add this method to your existing JwtUtility class

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // If you don't already have the extractClaim method:
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSecretKey())
                .parseClaimsJws(token)
                .getBody();
    }
}