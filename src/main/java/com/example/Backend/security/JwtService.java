package com.example.Backend.security;

import com.example.Backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;        // đơn vị millisecond (86400000 = 24 giờ)

    /**
     * Tạo khóa ký JWT từ secret (HS256)
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Tạo JWT từ đối tượng User
     * - Subject = email
     * - Claims = chứa role và các thông tin khác
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().name());      // ADMIN, STUDENT, TEACHER, COMPANY
        claims.put("provider", user.getProvider().name());
        return Jwts.builder()
                .setClaims(claims)                      // lưu role vào payload
                .setSubject(user.getEmail())            // subject thường là email
                .setIssuedAt(new Date())                // thời gian tạo
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ===================================================================
    // Các hàm extract thông tin từ token
    // ===================================================================

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Lấy role từ token (để phân quyền)
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);   // nếu parse thành công → token hợp lệ
            return !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Hàm gốc để lấy toàn bộ Claims từ token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

