package com.example.tour_management.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private static final String SECRET_KEY =
            "SECRET_KEY_256_BIT_FOR_TOUR_MANAGEMENT_123456";

    private static final long EXPIRATION =
            24 * 60 * 60 * 1000; // 1 ngày

    private final Key key =
            Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    public String generateToken(UserDetails userDetails, String fullName) {

        log.info("Tạo JWT cho user: {}", userDetails.getUsername());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("username", fullName)
                .claim(
                        "roles",
                        userDetails.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + EXPIRATION)
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return getClaims(token).getSubject();
        } catch (Exception e) {
            log.error("Không thể lấy username từ token: {}", e.getMessage());
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);

            boolean isValid = username != null &&
                    username.equals(userDetails.getUsername()) &&
                    !isTokenExpired(token);

            if (!isValid) {
                log.warn("Token không hợp lệ cho user: {}", userDetails.getUsername());
            }

            return isValid;

        } catch (Exception e) {
            log.error("Lỗi khi validate token: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return getClaims(token)
                    .getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            log.warn("Token đã hết hạn hoặc không hợp lệ");
            return true;
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            log.error("Token không hợp lệ: {}", e.getMessage());
            throw e;
        }
    }
}