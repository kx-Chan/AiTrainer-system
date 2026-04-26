package com.aitrainer.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

/**
 * JWT 令牌处理工具类。
 */
@Slf4j
@Component
public final class JwtUtils {

    private final Key key;
    private final long expiration;

    /**
     * JwtUtils 构造函数。
     *
     * @param secret     JWT 密钥。
     * @param expiration JWT 过期时间（毫秒）。
     */
    public JwtUtils(@Value("${jwt.secret}") final String secret, @Value("${jwt.expiration}") final long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    /**
     * 为指定用户生成 JWT 令牌。
     *
     * @param userId   用户 ID。
     * @param username 用户名。
     * @return JWT 令牌。
     */
    public String generateToken(final Long userId, final String username) {
        return generateToken(userId, username, 0);
    }

    /**
     * 为指定用户生成 JWT 令牌（带 tokenVersion）。
     *
     * @param userId       用户 ID。
     * @param username     用户名。
     * @param tokenVersion Token 版本号。
     * @return JWT 令牌。
     */
    public String generateToken(final Long userId, final String username, final Integer tokenVersion) {
        log.info("正在为用户 {} (ID: {}) 生成令牌，版本: {}", username, userId, tokenVersion);
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("tokenVersion", tokenVersion != null ? tokenVersion : 0)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证 JWT 令牌是否有效。
     *
     * @param token JWT 令牌。
     * @return 如果有效返回 true，否则返回 false。
     */
    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (final Exception e) {
            log.error("令牌验证失败", e);
            return false;
        }
    }

    /**
     * 从 JWT 令牌中提取用户名。
     *
     * @param token JWT 令牌。
     * @return 用户名。
     */
    public String getUsernameFromToken(final String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    /**
     * 从 JWT 令牌中提取用户 ID。
     *
     * @param token JWT 令牌。
     * @return 用户 ID。
     */
    public Long getUserIdFromToken(final String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("userId", Long.class);
    }

    /**
     * 从 JWT 令牌中提取 Token 版本号。
     *
     * @param token JWT 令牌。
     * @return Token 版本号，默认为 0。
     */
    public Integer getTokenVersionFromToken(final String token) {
        Integer version = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("tokenVersion", Integer.class);
        return version != null ? version : 0;
    }
}
